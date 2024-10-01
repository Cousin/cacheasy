package com.joeyexecutive.cacheasy.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.*;

@SupportedAnnotationTypes("com.joeyexecutive.cacheasy.annotation.Cached")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class CacheasyAnnotationProcessor extends AbstractProcessor {

    //TODO
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

}