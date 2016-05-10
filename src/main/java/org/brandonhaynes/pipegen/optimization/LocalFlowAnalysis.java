package org.brandonhaynes.pipegen.optimization;

import soot.Local;
import soot.RefLikeType;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LocalFlowAnalysis extends ForwardFlowAnalysis
{
    protected static final Object UNKNOWN = new Object() {
        public String toString() {
            return "UNKNOWN";
        }
    };

    protected Set<Local> locals;

    public LocalFlowAnalysis(UnitGraph g)
    {
        super(g);
        locals = new HashSet<Local>(); locals.addAll(g.getBody().getLocals());

        for (Local l : (Collection<Local>) g.getBody().getLocals()) {
            if (l.getType() instanceof RefLikeType)
                locals.add(l);
        }

        doAnalysis();
    }

    protected void merge(Object in1, Object in2, Object o)
    {
        HashMap inMap1 = (HashMap) in1;
        HashMap inMap2 = (HashMap) in2;
        HashMap outMap = (HashMap) o;

        for (Local l : locals) {
            Set l1 = (Set)inMap1.get(l), l2 = (Set)inMap2.get(l);
            Set out = (Set)outMap.get(l);
            out.clear();
            if (l1.contains(UNKNOWN) || l2.contains(UNKNOWN)) {
                out.add(UNKNOWN);
            } else {
                out.addAll(l1); out.addAll(l2);
            }
        }
    }


    protected void flowThrough(Object inValue, Object unit,
                               Object outValue)
    {
        HashMap     in  = (HashMap) inValue;
        HashMap     out = (HashMap) outValue;
        Stmt    s   = (Stmt)    unit;

        out.clear();
        out.putAll(in);

        if (s instanceof DefinitionStmt) {
            DefinitionStmt ds = (DefinitionStmt) s;
            Value lhs = ds.getLeftOp();
            Value rhs = ds.getRightOp();
            if (lhs instanceof Local) {
                HashSet lv = new HashSet();
                out.put(lhs, lv);
                if (rhs instanceof NewExpr) {
                    lv.add(rhs);
                } else if (rhs instanceof Local) {
                    lv.addAll((HashSet)in.get(rhs));
                } else lv.add(UNKNOWN);
            }
        }
    }

    protected void copy(Object source, Object dest)
    {
        HashMap sourceMap = (HashMap) source;
        HashMap destMap   = (HashMap) dest;

        destMap.putAll(sourceMap);
    }

    protected Object entryInitialFlow()
    {
        HashMap m = new HashMap();
        for (Local l : (Collection<Local>) locals) {
            HashSet s = new HashSet(); s.add(UNKNOWN);
            m.put(l, s);
        }
        return m;
    }

    protected Object newInitialFlow()
    {
        HashMap m = new HashMap();
        for (Local l : (Collection<Local>) locals) {
            HashSet s = new HashSet();
            m.put(l, s);
        }
        return m;
    }

    /**
     * Returns true if this analysis has any information about local l
     * at statement s (i.e. it is not {@link #UNKNOWN}).
     * In particular, it is safe to pass in locals/statements that are not
     * even part of the right method. In those cases <code>false</code>
     * will be returned.
     * Permits s to be <code>null</code>, in which case <code>false</code> will be returned.
     */
    public boolean hasInfoOn(Local l, Stmt s) {
        HashMap flowBefore = (HashMap) getFlowBefore(s);
        if(flowBefore==null) {
            return false;
        } else {
            Set info = (Set) flowBefore.get(l);
            return info!=null && !info.contains(UNKNOWN);
        }
    }

    /**
     * @return true if values of l1 (at s1) and l2 (at s2) are known
     * to point to different objects
     */
    public boolean notMayAlias(Local l1, Stmt s1, Local l2, Stmt s2) {
        Set l1n = (Set) ((HashMap)getFlowBefore(s1)).get(l1);
        Set l2n = (Set) ((HashMap)getFlowBefore(s2)).get(l2);

        if (l1n.contains(UNKNOWN) || l2n.contains(UNKNOWN))
            return false;

        Set n = new HashSet(); n.addAll(l1n); n.retainAll(l2n);
        return n.isEmpty();
    }

}