package usuario.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Define a classe JwtRequestFilter, que estende OncePerRequestFilter
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    // Define propriedades para armazenar instâncias de JwtUtil e UserDetailsService
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Construtor que inicializa as propriedades com instâncias fornecidas
    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Lista de caminhos que não requerem autenticação
    private static final String[] PUBLIC_PATHS = {
        "/usuario/login",
        "/auth",
        "/usuario",
        "/usuarios"
    };

    // Verifica se a requisição atual deve ser ignorada pelo filtro
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // Remove o context path do caminho, se existir
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        
        log.debug("Verificando se o caminho é público: {}", path);
        
        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                log.debug("Caminho público encontrado: {}", publicPath);
                return true; // Não aplicar o filtro para rotas públicas
            }
        }
        
        log.debug("Caminho requer autenticação: {}", path);
        return false; // Aplicar o filtro para todas as outras rotas
    }

    // Método chamado uma vez por requisição para processar o filtro
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        log.debug("Processando requisição para: {}", path);

        // Obtém o valor do header "Authorization" da requisição
        final String authorizationHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authorizationHeader);

        // Verifica se o cabeçalho existe e começa com "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                // Extrai o token JWT do cabeçalho
                final String token = authorizationHeader.substring(7);
                log.debug("Token JWT extraído: {}", token);
                
                // Extrai o nome de usuário do token JWT
                final String username = jwtUtil.extractUsername(token);
                log.debug("Usuário extraído do token: {}", username);

                // Se o nome de usuário não for nulo e o usuário não estiver autenticado ainda
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("Carregando detalhes do usuário: {}", username);
                    // Carrega os detalhes do usuário a partir do nome de usuário
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Valida o token JWT
                    if (jwtUtil.validateToken(token, username)) {
                        log.debug("Token válido para o usuário: {}", username);
                        // Cria um objeto de autenticação com as informações do usuário
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        
                        // Define a autenticação no contexto de segurança
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Usuário autenticado com sucesso: {}", username);
                    } else {
                        log.warn("Token inválido para o usuário: {}", username);
                    }
                }
            } catch (Exception e) {
                log.error("Erro ao processar o token JWT: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token inválido: " + e.getMessage());
                return;
            }
        } else {
            log.debug("Nenhum token JWT encontrado no cabeçalho Authorization");
            // Se for uma rota pública, permite a continuação sem autenticação
            for (String publicPath : PUBLIC_PATHS) {
                if (path.startsWith(publicPath)) {
                    log.debug("Rota pública acessada: {}", path);
                    chain.doFilter(request, response);
                    return;
                }
            }
            
            // Se não for uma rota pública e não tiver token, retorna não autorizado
            log.warn("Acesso não autorizado para a rota: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Acesso não autorizado");
            return;
        }

        // Continua a cadeia de filtros, permitindo que a requisição prossiga
        chain.doFilter(request, response);
    }
}
