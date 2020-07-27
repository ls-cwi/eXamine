package org.cwi.examine.internal.layout.dwyer.vpsc;

import java.util.ArrayList;
import java.util.List;

public class Block {
    public List<Variable> vars;
    public double posn;
    public PositionStats ps;
    public int blockInd;

    public Block(Variable v) {
        v.offset = 0;
        
        this.vars = new ArrayList<Variable>();
        this.ps = new PositionStats(v.scale);
        this.addVariable(v);
    }

    private void addVariable(Variable v) {
        v.block = this;
        vars.add(v);
        ps.addVariable(v);
        posn = ps.getPosn();
    }

    // move the block where it needs to be to minimize cost
    public void updateWeightedPosition() {
        ps.AB = ps.AD = ps.A2 = 0;
        for (int i = 0, n = vars.size(); i < n; ++i) ps.addVariable(vars.get(i));
        posn = ps.getPosn();
    }

    private double compute_lm(final Variable v, final Variable u, final ConstraintAction postAction) {
        final double[] dfdv = new double[]{ v.dfdv() };
        
        v.visitNeighbours(u, new Variable.VisitNeighbour() {

            @Override
            public void apply(Constraint c, Variable next) {
                double _dfdv = compute_lm(next, v, postAction);
                
                if (next == c.right) {
                    dfdv[0] += _dfdv * c.left.scale;
                    c.lm = _dfdv;
                } else {
                    dfdv[0] += _dfdv * c.right.scale;
                    c.lm = -_dfdv;
                }
                
                if(postAction != null) postAction.apply(c);
            }
            
        });
        
        return dfdv[0] / v.scale;
    }
    
    public static interface ConstraintAction {
        
        public void apply(Constraint c);
        
    }

    private void populateSplitBlock(final Variable v, final Variable prev) {
        v.visitNeighbours(prev, new Variable.VisitNeighbour() {

            @Override
            public void apply(Constraint c, Variable next) {
                next.offset = v.offset + (next == c.right ? c.gap : -c.gap);
                addVariable(next);
                populateSplitBlock(next, v);
            }
            
        });
    }

    // traverse the active constraint tree applying visit to each active constraint
    public void traverse(final ConstraintAction visit, final List<Object> acc, final Variable v, final Variable prev) {
        v.visitNeighbours(prev, new Variable.VisitNeighbour() {

            @Override
            public void apply(Constraint c, Variable next) {
                //acc.add(visit(c)); // To do: fix?>
                traverse(visit, acc, next, v);
            }
            
        });
    }

    // calculate lagrangian multipliers on constraints and
    // find the active constraint in this block with the smallest lagrangian.
    // if the lagrangian is negative, then the constraint is a split candidate.  
    public Constraint findMinLM() {
        FindConstraintAction fca = new FindConstraintAction();
        compute_lm(vars.get(0), null, fca);
        
        return fca.m;
    }
    
    private static class FindConstraintAction implements ConstraintAction {
            
        public Constraint m = null;

        @Override
        public void apply(Constraint c) {
            if (!c.equality && (m == null || c.lm < m.lm)) m = c;
        }

    };

    private Constraint findMinLMBetween(Variable lv, Variable rv) {
        compute_lm(lv, null, null);
        
        FindBetweenPath betweenPath = new FindBetweenPath();
        
        findPath(lv, null, rv, betweenPath);
        
        return betweenPath.m;
    }
    
    private class FindBetweenPath implements Variable.VisitNeighbour {
        public Constraint m = null;        
        
        @Override
        public void apply(Constraint c, Variable next) {
            if (!c.equality &&
                c.right == next &&
                (m == null || c.lm < m.lm)) {
                    m = c;
            }
        }
        
    }

    private boolean findPath(
                             final Variable v,
                             final Variable prev,
                             final Variable to,
                             final Variable.VisitNeighbour visit) {
        VisitPath visitPath = new VisitPath(v, to, visit);
        
        v.visitNeighbours(prev, visitPath);
        
        return visitPath.endFound;
    }
    
    private class VisitPath implements Variable.VisitNeighbour {
        final Variable v, to;
        final Variable.VisitNeighbour visit;
        public boolean endFound = false;

        public VisitPath(Variable v, Variable to, Variable.VisitNeighbour visit) {
            this.v = v;
            this.to = to;
            this.visit = visit;
        }
        
        @Override
        public void apply(Constraint c, Variable next) {
            if (!endFound && (next == to || findPath(next, v, to, visit))) {
                endFound = true;
                visit.apply(c, next);
            }
        }
        
    }

    // Search active constraint tree from u to see if there is a directed path to v.
    // Returns true if path is found.
    public boolean isActiveDirectedPathBetween(Variable u, Variable v) {
        if (u == v) return true;
        
        for (int i = u.cOut.size() - 1; i >= 0; i--) {
            Constraint c = u.cOut.get(i);
            if (c.active && this.isActiveDirectedPathBetween(c.right, v))
                return true;
        }
        
        return false;
    }

    // split the block into two by deactivating the specified constraint
    public static Block[] split(Constraint c) {
        c.active = false;
        return new Block[] {Block.createSplitBlock(c.left),
                            Block.createSplitBlock(c.right)};
    }

    private static Block createSplitBlock(Variable startVar) {
        Block b = new Block(startVar);
        b.populateSplitBlock(startVar, null);
        
        return b;
    }

    // find a split point somewhere between the specified variables
    public VariableSplit splitBetween(Variable vl, Variable vr) {
        Constraint c = findMinLMBetween(vl, vr);
        if (c != null) {
            Block[] bs = Block.split(c);
            return new VariableSplit(c, bs[0], bs[1]);
        }
        
        // couldn't find a split point - for example the active path is all equality constraints
        return null;
    }
    
    public static class VariableSplit {
        public Constraint constraint;
        public Block lb, rb;
        
        public VariableSplit(Constraint constraint, Block lb, Block rb) {
            this.constraint = constraint;
            this.lb = lb;
            this.rb = rb;
        }
        
    }

    public void mergeAcross(Block b, Constraint c, double dist) {
        c.active = true;
        for (Variable v: b.vars) {
            v.offset += dist;
            addVariable(v);
        }
        posn = ps.getPosn();
    }

    public double cost() {
        double sum = 0;
        
        for(Variable v: vars) {
            double d = v.position() - v.desiredPosition;
            sum += d * d * v.weight;
        }
            
        return sum;
    }
    
}

/*
    export class Block {
        vars: Variable[] = [];
        posn: number;
        ps: PositionStats;
        blockInd: number;

        constructor(v: Variable) {
            v.offset = 0;
            this.ps = new PositionStats(v.scale);
            this.addVariable(v);
        }

        private addVariable(v: Variable): void {
            v.block = this;
            this.vars.push(v);
            this.ps.addVariable(v);
            this.posn = this.ps.getPosn();
        }

        // move the block where it needs to be to minimize cost
        updateWeightedPosition(): void {
            this.ps.AB = this.ps.AD = this.ps.A2 = 0;
            for (var i = 0, n = this.vars.length; i < n; ++i)
                this.ps.addVariable(this.vars[i]);
            this.posn = this.ps.getPosn();
        }

        private compute_lm(v: Variable, u: Variable, postAction: (c: Constraint)=>void): number {
            var dfdv = v.dfdv();
            v.visitNeighbours(u, (c, next) => {
                var _dfdv = this.compute_lm(next, v, postAction);
                if (next === c.right) {
                    dfdv += _dfdv * c.left.scale;
                    c.lm = _dfdv;
                } else {
                    dfdv += _dfdv * c.right.scale;
                    c.lm = -_dfdv;
                }
                postAction(c);
            });
            return dfdv / v.scale;
        }
        
        private populateSplitBlock(v: Variable, prev: Variable): void {
            v.visitNeighbours(prev, (c, next) => {
                next.offset = v.offset + (next === c.right ? c.gap : -c.gap);
                this.addVariable(next);
                this.populateSplitBlock(next, v);
            });
        }

        // traverse the active constraint tree applying visit to each active constraint
        traverse(visit: (c: Constraint) => any, acc: any[], v: Variable = this.vars[0], prev: Variable=null) {
            v.visitNeighbours(prev, (c, next) => {
                acc.push(visit(c));
                this.traverse(visit, acc, next, v);
            });
        }

        // calculate lagrangian multipliers on constraints and
        // find the active constraint in this block with the smallest lagrangian.
        // if the lagrangian is negative, then the constraint is a split candidate.  
        findMinLM(): Constraint {
            var m: Constraint = null;
            this.compute_lm(this.vars[0], null, c=> {
                if (!c.equality && (m === null || c.lm < m.lm)) m = c;
            });
            return m;
        }

        private findMinLMBetween(lv: Variable, rv: Variable): Constraint {
            this.compute_lm(lv, null, () => {});
            var m = null;
            this.findPath(lv, null, rv, (c, next)=> {
                if (!c.equality && c.right === next && (m === null || c.lm < m.lm)) m = c;
            });
            return m;
        }

        private findPath(v: Variable, prev: Variable, to: Variable, visit: (c: Constraint, next:Variable)=>void): boolean {
            var endFound = false;
            v.visitNeighbours(prev, (c, next) => {
                if (!endFound && (next === to || this.findPath(next, v, to, visit)))
                {
                    endFound = true;
                    visit(c, next);
                }
            });
            return endFound;
        }
        
        // Search active constraint tree from u to see if there is a directed path to v.
        // Returns true if path is found.
        isActiveDirectedPathBetween(u: Variable, v: Variable) : boolean {
            if (u === v) return true;
            var i = u.cOut.length;
            while(i--) {
                var c = u.cOut[i];
                if (c.active && this.isActiveDirectedPathBetween(c.right, v))
                    return true;
            }
            return false;
        }

        // split the block into two by deactivating the specified constraint
        static split(c: Constraint): Block[]{

            c.active = false;
            return [Block.createSplitBlock(c.left), Block.createSplitBlock(c.right)];
        }

        private static createSplitBlock(startVar: Variable): Block {
            var b = new Block(startVar);
            b.populateSplitBlock(startVar, null);
            return b;
        }

        // find a split point somewhere between the specified variables
        splitBetween(vl: Variable, vr: Variable): { constraint: Constraint; lb: Block; rb: Block } {

            var c = this.findMinLMBetween(vl, vr);
            if (c !== null) {
                var bs = Block.split(c);
                return { constraint: c, lb: bs[0], rb: bs[1] };
            }
            // couldn't find a split point - for example the active path is all equality constraints
            return null;
        }

        mergeAcross(b: Block, c: Constraint, dist: number): void {
            c.active = true;
            for (var i = 0, n = b.vars.length; i < n; ++i) {
                var v = b.vars[i];
                v.offset += dist;
                this.addVariable(v);
            }
            this.posn = this.ps.getPosn();
        }

        cost(): number {
            var sum = 0, i = this.vars.length;
            while (i--) {
                var v = this.vars[i],
                    d = v.position() - v.desiredPosition;
                sum += d * d * v.weight;
            }
            return sum;
        }
    }
 */