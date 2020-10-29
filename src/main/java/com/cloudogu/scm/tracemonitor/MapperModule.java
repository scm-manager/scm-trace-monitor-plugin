package com.cloudogu.scm.tracemonitor;

import com.cloudogu.scm.tracemonitor.config.ConfigurationMapper;
import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.plugin.Extension;

@Extension
public class MapperModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ConfigurationMapper.class).to(Mappers.getMapperClass(ConfigurationMapper.class));
  }
}
