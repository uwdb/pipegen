package org.brandonhaynes.pipegen.mutation.rules;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;

import java.io.IOException;

public interface Rule {
  	boolean isApplicable(TraceResult trace);
   	boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException;
}
