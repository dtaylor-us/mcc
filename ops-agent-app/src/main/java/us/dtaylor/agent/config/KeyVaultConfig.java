//package us.dtaylor.agent.config;
//
//import com.azure.identity.DefaultAzureCredentialBuilder;
//import com.azure.security.keyvault.secrets.SecretClient;
//import com.azure.security.keyvault.secrets.SecretClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class KeyVaultConfig {
//    @Bean
//    public SecretClient secretClient(@Value("${KEY_VAULT_URI}") String vaultUri) {
//        return new SecretClientBuilder()
//                .vaultUrl(vaultUri)
//                .credential(new DefaultAzureCredentialBuilder().build())
//                .buildClient();
//    }
//}
//
