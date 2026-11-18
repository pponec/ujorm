package org.ujorm.core.stale;

/** A domain class whose pre-compiled handler is outdated */
public record StaleDomain(Long id, String name) {}
