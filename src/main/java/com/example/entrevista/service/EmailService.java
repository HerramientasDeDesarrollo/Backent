package com.example.entrevista.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.email.from:maracuya433@gmail.com}")
    private String fromEmail;
    
    @Value("${app.email.fromName:Plataforma de Entrevistas}")
    private String fromName;
    
    public void sendVerificationEmail(String toEmail, String verificationCode, String userName, String userType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Verificación de cuenta - Plataforma de Entrevistas");
            
            String htmlContent = buildVerificationEmailContent(userName, verificationCode, userType);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Email de verificación enviado exitosamente a: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Error al enviar email de verificación a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error enviando email de verificación: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al enviar email a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error interno enviando email");
        }
    }
    
    private String buildVerificationEmailContent(String userName, String code, String userType) {
        String userTypeDisplay = userType.equals("USUARIO") ? "candidato" : "empresa";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verificación de Email</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Arial', sans-serif; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
                    
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: bold;">
                            ¡Bienvenido %s! 🎉
                        </h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 10px 0 0 0; font-size: 16px;">
                            Te registraste como %s en nuestra plataforma
                        </p>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 40px 30px;">
                        <h2 style="color: #333; margin: 0 0 20px 0; font-size: 24px;">
                            Verificación de Email 📧
                        </h2>
                        
                        <p style="color: #666; line-height: 1.6; font-size: 16px; margin-bottom: 30px;">
                            Para completar tu registro y comenzar a usar nuestra plataforma de entrevistas con IA, 
                            necesitamos verificar tu dirección de email. Ingresa el siguiente código:
                        </p>
                        
                        <!-- Verification Code Box -->
                        <div style="background: linear-gradient(135deg, #f8f9ff 0%%, #e8f0ff 100%%); 
                                    border: 2px dashed #667eea; 
                                    border-radius: 12px; 
                                    padding: 30px; 
                                    text-align: center; 
                                    margin: 30px 0;">
                            <p style="color: #667eea; margin: 0 0 10px 0; font-size: 14px; font-weight: bold;">
                                TU CÓDIGO DE VERIFICACIÓN
                            </p>
                            <h1 style="color: #667eea; 
                                       font-size: 42px; 
                                       margin: 0; 
                                       letter-spacing: 8px; 
                                       font-weight: bold;
                                       text-shadow: 0 2px 4px rgba(102, 126, 234, 0.1);">
                                %s
                            </h1>
                        </div>
                        
                        <!-- Important Info -->
                        <div style="background: #fff3cd; 
                                    border: 1px solid #ffeaa7; 
                                    border-radius: 8px; 
                                    padding: 20px; 
                                    margin: 25px 0;">
                            <p style="color: #856404; margin: 0; font-size: 14px;">
                                <strong>⚠️ Importante:</strong> Este código expira en <strong>15 minutos</strong> 
                                por razones de seguridad.
                            </p>
                        </div>
                        
                        <p style="color: #666; line-height: 1.6; font-size: 14px; margin-top: 30px;">
                            Si no solicitaste este registro, puedes ignorar este email de forma segura.
                        </p>
                        
                        <!-- Next Steps -->
                        <div style="margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px;">
                            <h3 style="color: #333; margin: 0 0 15px 0; font-size: 18px;">¿Qué sigue? 🚀</h3>
                            <ul style="color: #666; margin: 0; padding-left: 20px; line-height: 1.6;">
                                <li>Ingresa el código en la página de verificación</li>
                                <li>¡Tu cuenta será activada inmediatamente!</li>
                                <li>Podrás acceder a todas las funciones de la plataforma</li>
                            </ul>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background: #f8f9fa; 
                                padding: 30px; 
                                text-align: center; 
                                border-top: 1px solid #e9ecef;">
                        <p style="color: #6c757d; margin: 0; font-size: 12px;">
                            © 2025 Plataforma de Entrevistas con IA<br>
                            Sistema inteligente de reclutamiento y evaluación
                        </p>
                        <p style="color: #6c757d; margin: 10px 0 0 0; font-size: 11px;">
                            Este es un email automático, por favor no respondas.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, userTypeDisplay, code);
    }
    
    public void sendWelcomeEmail(String toEmail, String userName, String userType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("🎉 ¡Cuenta verificada exitosamente!");
            
            String userTypeDisplay = userType.equals("USUARIO") ? "candidato" : "empresa";
            
            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); padding: 30px; text-align: center; color: white;">
                        <h1>¡Bienvenido %s! ✅</h1>
                        <p>Tu cuenta como %s ha sido verificada exitosamente</p>
                    </div>
                    <div style="padding: 30px; background: #f8f9fa;">
                        <h2>¡Ya puedes comenzar! 🚀</h2>
                        <p>Tu email ha sido verificado correctamente. Ahora puedes:</p>
                        <ul>
                            <li>Iniciar sesión en tu cuenta</li>
                            <li>Acceder a todas las funciones</li>
                            <li>Comenzar a usar nuestra plataforma</li>
                        </ul>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="#" style="background: #10b981; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: bold;">
                                Iniciar Sesión
                            </a>
                        </div>
                    </div>
                </div>
                """, userName, userTypeDisplay);
                
            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("Email de bienvenida enviado a: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Error enviando email de bienvenida a {}: {}", toEmail, e.getMessage());
        }
    }
}
