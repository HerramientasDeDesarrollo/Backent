package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidoMaterno;
    private String apellidoPaterno;
    @Column(unique = true)
    private String dni;
    @Column(unique = true)
    private int telefono;

    @Column(unique = true)
    private String correo;

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Postulacion> postulaciones;

    private String codigoSeguridad;

    @Temporal(TemporalType.TIMESTAMP)
    private Date codigoSeguridadExpiracion;

    private String rol;

    public String getRol() {
        return rol;
    }

    // Implementa los métodos de UserDetails:
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.getRol()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
