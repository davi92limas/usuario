package com.davi.usuario.infrastructure.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
@Builder
public class Usuario implements UserDetails {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private  long id;
   @Column(name = "nome", length = 100)
   private String nome;
   @Column(name = "email", length = 100)
   private String email;
   @Column(name = "senha")
   private String senha;
   @OneToMany(cascade = CascadeType.ALL)
   @JoinColumn(name = "usuario_id", referencedColumnName = "id")
   private List<Endereco> endereco;
   @OneToMany(cascade = CascadeType.ALL)
   @JoinColumn(name = "usuario_id", referencedColumnName = "id")
   private List<Telefone> telefone;

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return List.of();
   }

   @Override
   public String getPassword() {
      return senha;
   }

   @Override
   public String getUsername() {
      return email;
   }
}
