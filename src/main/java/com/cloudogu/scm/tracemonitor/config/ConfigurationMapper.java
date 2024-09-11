/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.tracemonitor.config;

import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import jakarta.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ConfigurationMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract GlobalConfig map(GlobalConfigDto dto);

  @Mapping(target = "attributes", ignore = true)
  public abstract GlobalConfigDto map(GlobalConfig config);

  @AfterMapping
  void appendLinks(@MappingTarget GlobalConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (SecurityUtils.getSubject().isPermitted("configuration:write:traceMonitor")) {
      linksBuilder.single(link("update", update()));
    }
    target.add(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), GlobalConfigResource.class);
    return linkBuilder.method("get").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), GlobalConfigResource.class);
    return linkBuilder.method("update").parameters().href();
  }
}
