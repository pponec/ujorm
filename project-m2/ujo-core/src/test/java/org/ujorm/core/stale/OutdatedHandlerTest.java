package org.ujorm.core.stale;

import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.generator.ClassGenerator;
import org.ujorm.core.generator.ClassName;
import org.ujorm.core.generator.DomainModel;
import org.ujorm.core.generator.JavaSourceGenerator;
import org.ujorm.gen_.org.ujorm.core.stale.LegacyDomain_;
import org.ujorm.gen_.org.ujorm.core.stale.StaleDomain_;

import static org.junit.jupiter.api.Assertions.*;

/** Tests of the detection of an outdated pre-compiled handler */
class OutdatedHandlerTest {

    @Test
    void outdatedHandlerIsGeneratedAgain() {
        var handler = DomainHandlerService.of().getHandler(StaleDomain.class);
        assertNotSame(StaleDomain_.class, handler.getClass());
        assertEquals(StaleDomain_.class.getName(), handler.getClass().getName());
        assertEquals("[id, name]", handler.getKeyList().toString());
        assertEquals(new StaleDomain(1L, "A"), handler.newDomain(1L, "A"));
    }

    @Test
    void handlerWithoutHashIsGeneratedAgain() {
        var handler = DomainHandlerService.of().getHandler(LegacyDomain.class);
        assertNotSame(LegacyDomain_.class, handler.getClass());
        assertEquals("[id, name]", handler.getKeyList().toString());
    }

    /** The source code carries the hash of the model, so a current handler is recognized as up-to-date. */
    @Test
    void currentHandlerHasModelHash() throws Exception {
        var className = ClassName.ofGenerated(StaleDomain.class);
        var generator = new JavaSourceGenerator();
        var model = DomainModel.of(StaleDomain.class);
        var hash = generator.getModelHash(model, className);
        assertEquals(16, hash.length());

        var source = generator.getSourceCode(model, className);
        assertTrue(source.contains("public static final String MODEL_HASH = \"%s\";".formatted(hash)), source);

        var handlerClass = new ClassGenerator().createClass(source, className);
        assertEquals(hash, handlerClass.getField(JavaSourceGenerator.MODEL_HASH_FIELD).get(null));
        assertNotSame(StaleDomain_.class, handlerClass, "The compiled class has a precedence over the parent");
    }
}
