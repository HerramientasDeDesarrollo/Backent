import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Cambia "tuNuevaContraseña" por la contraseña que quieras hashear
        String plainPassword = "password123";
        String hashedPassword = encoder.encode(plainPassword);
        
        System.out.println("Contraseña original: " + plainPassword);
        System.out.println("Hash generado: " + hashedPassword);
        
        // Verificar que el hash es correcto
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("Verificación exitosa: " + matches);
    }
}
