package org.cwi.examine.internal.layout.dwyer.vpsc;

import java.util.ArrayList;
import java.util.List;

public class Solver {
    public Blocks bs;
    public List<Constraint> inactive;
    public Variable[] vs;
    public Constraint[] cs;

    public static final double LAGRANGIAN_TOLERANCE = -1e-4;
    public static final double ZERO_UPPERBOUND = -1e-10;

    public Solver(Variable[] vs, Constraint[] cs) {
        this.vs = vs;
        this.cs = cs;
        
        for(Variable v: vs) {
            v.cIn = new ArrayList<Constraint>();
            v.cOut = new ArrayList<Constraint>();
        }
        
        for(Constraint c: cs) {
            c.left.cOut.add(c);
            c.right.cIn.add(c);
        }
        
        makeAllInactive();
        
        this.bs = null;
    }
    
    private void makeAllInactive() {
        inactive = new ArrayList<Constraint>();
        for(Constraint c: cs) {
            c.active = false;
            inactive.add(c);
        }
    }

    public double cost() {
        return bs.cost();
    }

    // set starting positions without changing desired positions.
    // Note: it throws away any previous block structure.
    public void setStartingPositions(final double[] ps) {
        makeAllInactive();
        
        this.bs = new Blocks(this.vs);
        this.bs.forEach(new Blocks.BlockFunction() {

            @Override
            public void apply(Block b, int i) {
                b.posn = ps[i];
            }
            
        });
    }

    public void setDesiredPositions(double[] ps) {
        for(int i = 0; i < vs.length; i++) {
            vs[i].desiredPosition = ps[i];
        }
    }

    private Constraint mostViolated() {
        double minSlack = Double.MAX_VALUE;
        Constraint v = null;
        List<Constraint> l = this.inactive;
        for (int i = 0; i < l.size(); ++i) {
            Constraint c = l.get(i);
            
            if (c.unsatisfiable) continue;
            
            double slack = c.slack();
            if (c.equality || slack < minSlack) {
                minSlack = slack;
                v = c;
                if (c.equality) break;
            }
        }
        
        if (v != null &&
            (minSlack < Solver.ZERO_UPPERBOUND && !v.active || v.equality)) {
            l.remove(v);
        }
        
        return v;
    }

    // satisfy constraints by building block structure over violated constraints
    // and moving the blocks to their desired positions
    public void satisfy() {
        if (bs == null) {
            bs = new Blocks(vs);
        }

        bs.split(inactive);
        
        Constraint v = mostViolated();
        while (v != null &&
               (v.equality || v.slack() < Solver.ZERO_UPPERBOUND && !v.active)) {
            Block lb = v.left.block;
            Block rb = v.right.block;

            if (lb != rb) {
                bs.merge(v);
            } else {
                if (lb.isActiveDirectedPathBetween(v.right, v.left)) {
                    // cycle found!
                    v.unsatisfiable = true;
                    continue;
                }
                
                // constraint is within block, need to split first
                Block.VariableSplit split = lb.splitBetween(v.left, v.right);
                if (split != null) {
                    bs.insert(split.lb);
                    bs.insert(split.rb);
                    bs.remove(lb);
                    inactive.add(split.constraint);
                } else {
                    v.unsatisfiable = true;
                    continue;
                }
                if (v.slack() >= 0) {
                    // v was satisfied by the above split!
                    this.inactive.add(v);
                } else {
                    this.bs.merge(v);
                }
            }

            v = mostViolated();
        } // End while.
    }

    // repeatedly build and split block structure until we converge to an optimal solution
    public double solve() {
        satisfy();
        
        double lastcost = Double.MAX_VALUE;
        double cost = bs.cost();
        
        while (Math.abs(lastcost - cost) > 0.0001) {
            satisfy();
            lastcost = cost;
            cost = bs.cost();
        }
        
        return cost;
    }
    
}

/*
    export class Solver {
        bs: Blocks;
        inactive: Constraint[];

        static LAGRANGIAN_TOLERANCE = -1e-4;
        static ZERO_UPPERBOUND = -1e-10;

        constructor(public vs: Variable[], public cs: Constraint[]) {
            this.vs = vs;
            vs.forEach(v => {
                v.cIn = [], v.cOut = [];

            });
            this.cs = cs;
            cs.forEach(c => {
                c.left.cOut.push(c);
                c.right.cIn.push(c);

            });
            this.inactive = cs.map(c=> { c.active = false; return c; });
            this.bs = null;
        }

        cost(): number {
            return this.bs.cost();
        }

        // set starting positions without changing desired positions.
        // Note: it throws away any previous block structure.
        setStartingPositions(ps: number[]): void {
            this.inactive = this.cs.map(c=> { c.active = false; return c; });
            this.bs = new Blocks(this.vs);
            this.bs.forEach((b, i) => b.posn = ps[i]);
        }

        setDesiredPositions(ps: number[]): void {
            this.vs.forEach((v, i) => v.desiredPosition = ps[i]);
        }

        private mostViolated(): Constraint {
            var minSlack = Number.MAX_VALUE,
                v: Constraint = null,
                l = this.inactive,
                n = l.length,
                deletePoint = n;
            for (var i = 0; i < n; ++i) {
                var c = l[i];
                if (c.unsatisfiable) continue;
                var slack = c.slack();
                if (c.equality || slack < minSlack) {
                    minSlack = slack;
                    v = c;
                    deletePoint = i;
                    if (c.equality) break;
                }
            }
            if (deletePoint !== n &&
                (minSlack < Solver.ZERO_UPPERBOUND && !v.active || v.equality))
            {
                l[deletePoint] = l[n - 1];
                l.length = n - 1;
            }
            return v;
        }

        // satisfy constraints by building block structure over violated constraints
        // and moving the blocks to their desired positions
        satisfy(): void {
            if (this.bs == null) {
                this.bs = new Blocks(this.vs);
            }

            this.bs.split(this.inactive);
            var v: Constraint = null;
            while ((v = this.mostViolated()) && (v.equality || v.slack() < Solver.ZERO_UPPERBOUND && !v.active)) {
                var lb = v.left.block, rb = v.right.block;

                if (lb !== rb) {
                    this.bs.merge(v);
                } else {
                    if (lb.isActiveDirectedPathBetween(v.right, v.left)) {
                        // cycle found!
                        v.unsatisfiable = true;
                        continue;
                    }
                    // constraint is within block, need to split first
                    var split = lb.splitBetween(v.left, v.right);
                    if (split !== null) {
                        this.bs.insert(split.lb);
                        this.bs.insert(split.rb);
                        this.bs.remove(lb);
                        this.inactive.push(split.constraint);
                    } else {

                        v.unsatisfiable = true;
                        continue;
                    }
                    if (v.slack() >= 0) {

                        // v was satisfied by the above split!
                        this.inactive.push(v);
                    } else {

                        this.bs.merge(v);
                    }
                }

            }

        }

        // repeatedly build and split block structure until we converge to an optimal solution
        solve(): number {
            this.satisfy();
            var lastcost = Number.MAX_VALUE, cost = this.bs.cost();
            while (Math.abs(lastcost - cost) > 0.0001) {
                this.satisfy();
                lastcost = cost;
                cost = this.bs.cost();
            }
            return cost;
        }
    }
*/