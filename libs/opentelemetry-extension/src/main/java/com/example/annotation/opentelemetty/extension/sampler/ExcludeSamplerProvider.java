package com.example.annotation.opentelemetty.extension.sampler;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import lombok.extern.java.Log;

/** Sampler provider for {@link ExcludeSampler}. */
@AutoService(ConfigurableSamplerProvider.class)
@Log
public class ExcludeSamplerProvider implements ConfigurableSamplerProvider {
  @Override
  public Sampler createSampler(ConfigProperties config) {
    var sampler = new ExcludeSampler(config);
    log.info("Use \"" + getName() + "\" traces sampler");
    return sampler;
  }

  @Override
  public String getName() {
    return "exclude";
  }
}
