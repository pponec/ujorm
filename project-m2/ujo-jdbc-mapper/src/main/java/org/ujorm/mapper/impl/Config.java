package org.ujorm.mapper.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
//import org.ujorm.mapper.MapperContext;

/** TODO:pop: create interface from this class */
@Setter @Getter @ToString
public class Config /*implements MapperContext*/ {

    /** The first key in the sequence represents the primary key. */
    boolean firstPropertyIsIdentifier = true;


}
