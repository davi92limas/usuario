package com.davi.usuario.controller;

import com.davi.usuario.business.UsuarioService;
import com.davi.usuario.business.dto.UsuarioDTO;
import com.davi.usuario.infrastructure.repository.UsuarioRepository;
import com.davi.usuario.infrastructure.repository.exceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<UsuarioDTO> salvaUsuario(@RequestBody UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        return ResponseEntity.ok(usuarioService.salvaUsuario(usuarioDTO));
    }

    //Verefica se o email existe caso exista gera uma exception
    public  void  emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if (existe){
                throw  new ConflictException("Email já cadastrado" + email);
            }
        }catch (ConflictException e){
            throw  new ConflictException("Email já cadastrado" + e.getCause());
        }
    }
    //Chama o metedo
    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }
}

