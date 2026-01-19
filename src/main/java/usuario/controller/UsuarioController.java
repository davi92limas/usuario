package usuario.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import usuario.business.UsuarioService;
import usuario.business.dto.UsuarioDTO;
import usuario.infrastructure.security.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {


    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody UsuarioDTO usuarioDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usuarioDTO.getEmail(), usuarioDTO.getSenha()));
        return jwtUtil.generateToken(authentication.getName());

    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> salvarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.salvarUsuario(usuarioDTO));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> buscarTodosUsuarios() {
        return ResponseEntity.ok(usuarioService.buscarTodosUsuarios());
    }

    @GetMapping("/{email}")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorEmail(email));
    }

    @PutMapping("/{email}")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable String email, @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(email, usuarioDTO));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable String email) {
        usuarioService.deletarUsuario(email);
        return ResponseEntity.noContent().build();
    }
}

