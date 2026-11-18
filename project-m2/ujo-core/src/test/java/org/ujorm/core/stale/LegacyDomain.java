package org.ujorm.core.stale;

/** A domain class whose pre-compiled handler has no model hash */
public record LegacyDomain(Long id, String name) {}
