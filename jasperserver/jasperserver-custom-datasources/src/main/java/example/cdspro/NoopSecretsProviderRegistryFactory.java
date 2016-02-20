package example.cdspro;

import java.util.Collections;
import java.util.List;

import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;
import net.sf.jasperreports.util.SecretsProvider;
import net.sf.jasperreports.util.SecretsProviderFactory;

/**
 * This is a factory which creates an extension registry which creates a provider factory which creates a provider which creates a secret.
 * When asked for {@link SecretsProviderFactory} this eventually returns a secret provider which just returns whatever value is passed to it as the parameter in {@link SecretsProvider#getSecret(String)}.
 */
public class NoopSecretsProviderRegistryFactory implements ExtensionsRegistryFactory {

    private static final ExtensionsRegistry noopSecretsProviderExtensionsRegistry = new NoopSecretsProviderExtensionsRegistry();

    @Override
    public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {
        return noopSecretsProviderExtensionsRegistry;
    }

    private static final class NoopSecretsProviderExtensionsRegistry implements ExtensionsRegistry {

        private static final NoopSecretsProviderFactory noopSecretsProviderFactory = new NoopSecretsProviderFactory();

        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> getExtensions(Class<T> extensionType) {
            return (List<T>) (SecretsProviderFactory.class.equals(extensionType) ? Collections.singletonList(noopSecretsProviderFactory) : null);
        }
    }

    private static final class NoopSecretsProviderFactory implements SecretsProviderFactory {

        private static final SecretsProvider noopSecretsProvider = new NoopSecretsProvider();

        @Override
        public SecretsProvider getSecretsProvider(String category) {
            return noopSecretsProvider;
        }
    }

    private static final class NoopSecretsProvider implements SecretsProvider {

        @Override
        public String getSecret(String key) {
            return key;
        }

        @Override
        public boolean hasSecret(String key) {
            return true;
        }
    }
}
