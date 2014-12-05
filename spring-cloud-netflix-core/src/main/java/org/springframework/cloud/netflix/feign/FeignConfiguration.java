package org.springframework.cloud.netflix.feign;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.ribbon.LoadBalancingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.RibbonClientPreprocessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.netflix.archaius.ConfigurableEnvironmentConfiguration;

import javax.inject.Inject;
import java.net.URI;

/**
 * @author Spencer Gibb
 */
@Configuration
public class FeignConfiguration {
    @Autowired
    ConfigurableEnvironmentConfiguration envConfig; //FIXME: howto enforce this?

    @Autowired
    RibbonClientPreprocessor ribbonClientPreprocessor;

    @Autowired
    Decoder decoder;

    @Autowired
    Encoder encoder;

    @Autowired
    Logger logger;

    @Autowired
    Contract contract;

	@Autowired(required = false)
	Logger.Level logLevel;

	@Autowired(required = false)
	Retryer retryer;

	@Autowired(required = false)
	ErrorDecoder errorDecoder;

	@Autowired(required = false)
	Request.Options options;

    @Autowired(required = false)
    Client ribbonClient;

    protected Feign.Builder feign() {
		Feign.Builder builder = Feign.builder()
				//required values
				.logger(logger)
				.encoder(encoder)
				.decoder(decoder)
				.contract(contract);

		//optional values
		if (logLevel != null)
			builder.logLevel(logLevel);
		if (retryer != null)
			builder.retryer(retryer);
		if (errorDecoder != null)
			builder.errorDecoder(errorDecoder);
		if (options != null)
			builder.options(options);

		return builder;
    }

    protected <T> T loadBalance(Class<T> type, String schemeName) {
        return loadBalance(feign(), type, schemeName);
    }

    protected <T> T loadBalance(Feign.Builder builder, Class<T> type, String schemeName) {
        String name = URI.create(schemeName).getHost();
        ribbonClientPreprocessor.preprocess(name);

        if(ribbonClient != null) {
            return builder.client(ribbonClient).target(type, schemeName);
        } else {
            return builder.target(LoadBalancingTarget.create(type, schemeName));
        }
    }

}
