package usuario.business;

import com.davi.usuario.business.converter.UsuarioConverter;
import com.davi.usuario.business.dto.UsuarioDTO;
import com.davi.usuario.infrastructure.entity.Usuario;
import com.davi.usuario.infrastructure.exceptions.ConflitExeption;
import com.davi.usuario.infrastructure.exceptions.ResourceNotFoundExeception;
import com.davi.usuario.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDTO salvarUsuario(UsuarioDTO usuarioDTO) {
        log.info("Iniciando salvamento do usuário: {}", usuarioDTO.getEmail());
        try {
            emailExiste(usuarioDTO.getEmail());
            log.info("Email verificado, não existe no sistema");
            
            String senhaCriptografada = passwordEncoder.encode(usuarioDTO.getSenha());
            usuarioDTO.setSenha(senhaCriptografada);
            log.debug("Senha criptografada com sucesso");

            log.debug("Convertendo DTO para entidade");
            Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
            log.debug("Usuário convertido: {}", usuario);

            log.debug("Salvando usuário no banco de dados");
            usuario = usuarioRepository.save(usuario);
            log.info("Usuário salvo com ID: {}", usuario.getId());

            log.debug("Convertendo entidade para DTO");
            UsuarioDTO usuarioSalvoDTO = usuarioConverter.paraUsuarioDTO(usuario);
            log.debug("Conversão para DTO concluída");

            return usuarioSalvoDTO;
        } catch (Exception e) {
            log.error("Erro ao salvar usuário: {}", e.getMessage(), e);
            throw e;
        }
    }
    public void emailExiste(String email) {
        log.debug("Verificando se o email já existe: {}", email);
        try {
            boolean existe = verificarEmailExiste(email);
            if (existe) {
                String mensagem = String.format("Email %s já cadastrado no sistema", email);
                log.warn(mensagem);
                throw new ConflitExeption(mensagem);
            }
            log.debug("Email {} não existe no sistema", email);
        } catch (Exception e) {
            log.error("Erro ao verificar email: {}", e.getMessage(), e);
            throw new ConflitExeption("Erro ao verificar email: " + e.getMessage(), e);
        }
    }

    public boolean verificarEmailExiste(String email) {
        log.debug("Buscando email no banco de dados: {}", email);
        boolean existe = usuarioRepository.existsByEmail(email);
        log.debug("Email {} existe no banco de dados: {}", email, existe);
        return existe;
    }

    public List<UsuarioDTO> buscarTodosUsuarios() {
        log.info("Buscando todos os usuários");
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            log.info("Encontrados {} usuários", usuarios.size());
            return usuarios.stream()
                    .map(usuarioConverter::paraUsuarioDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Erro ao buscar usuários: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        log.info("Buscando usuário com email: {}", email);
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        String mensagem = String.format("Usuário com email %s não encontrado", email);
                        log.warn(mensagem);
                        return new ResourceNotFoundExeception(mensagem);
                    });
            log.info("Usuário encontrado: {}", usuario.getEmail());
            return usuarioConverter.paraUsuarioDTO(usuario);
        } catch (Exception e) {
            log.error("Erro ao buscar usuário: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deletarUsuario(String email) {
        log.info("Deletando usuário com email: {}", email);
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        String mensagem = String.format("Usuário com email %s não encontrado", email);
                        log.warn(mensagem);
                        return new ResourceNotFoundExeception(mensagem);
                    });
            
            usuarioRepository.deleteByEmail(email);
            log.info("Usuário deletado com sucesso: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Erro ao deletar usuário: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UsuarioDTO atualizarUsuario(String email, UsuarioDTO usuarioDTO) {
        log.info("Atualizando usuário com email: {}", email);
        try {
            Usuario usuarioExistente = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        String mensagem = String.format("Usuário com email %s não encontrado", email);
                        log.warn(mensagem);
                        return new ResourceNotFoundExeception(mensagem);
                    });

            // Verifica se o email foi alterado e se já existe
            if (!usuarioExistente.getEmail().equals(usuarioDTO.getEmail())) {
                log.debug("Email foi alterado, verificando se o novo email já existe");
                emailExiste(usuarioDTO.getEmail());
            }

            // Atualiza os dados
            usuarioExistente.setNome(usuarioDTO.getNome());
            usuarioExistente.setEmail(usuarioDTO.getEmail());
            
            // Só atualiza a senha se foi fornecida uma nova
            if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().isEmpty()) {
                log.debug("Atualizando senha do usuário");
                String senhaCriptografada = passwordEncoder.encode(usuarioDTO.getSenha());
                usuarioExistente.setSenha(senhaCriptografada);
            }

            // Atualiza telefones e endereços
            if (usuarioDTO.getTelefones() != null) {
                usuarioExistente.setTelefones(usuarioConverter.paraListaTelefone(usuarioDTO.getTelefones()));
            }
            
            if (usuarioDTO.getEnderecos() != null) {
                usuarioExistente.setEnderecos(usuarioConverter.paraListaEndereco(usuarioDTO.getEnderecos()));
            }

            Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);
            log.info("Usuário atualizado com sucesso: {}", usuarioAtualizado.getEmail());

            return usuarioConverter.paraUsuarioDTO(usuarioAtualizado);
        } catch (Exception e) {
            log.error("Erro ao atualizar usuário: {}", e.getMessage(), e);
            throw e;
        }
    }
}
