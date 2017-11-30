package org.cwi.examine.internal.layout.dwyer.vpsc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Blocks {
    private List<Block> list;
    public Variable[] vs;

    public Blocks(Variable[] vs) {
        this.vs = vs;
        
        int n = vs.length;
        this.list = new ArrayList<Block>();
        for(int i = 0; i < n; i++) {
            Block b = new Block(vs[i]);  // To do: is this an error?
            list.add(b);
            b.blockInd = i;
        }
    }

    public int cost() {
        int sum = 0;
        
        for(int i = 0; i < list.size(); i++) sum += list.get(i).cost();
        
        return sum;
    }

    public void insert(Block b) {
        b.blockInd = list.size();
        this.list.add(b);
    }

    public void remove(Block b) {
        this.list.remove(b);
    }

    // merge the blocks on either side of the specified constraint, by copying the smaller block into the larger
    // and deleting the smaller.
    public void merge(Constraint c) {
        Block l = c.left.block;
        Block r = c.right.block;

        double dist = c.right.offset - c.left.offset - c.gap;
        if (l.vars.size() < r.vars.size()) {
            r.mergeAcross(l, c, dist);
            remove(l);
        } else {
            l.mergeAcross(r, c, -dist);
            remove(r);
        }
    }

    public void forEach(BlockFunction f) {
        for(int i = 0; i < list.size(); i++) {
            f.apply(list.get(i), i);
        }
    }
    
    public static interface BlockFunction {
        public void apply(Block b, int i);
    }

    // useful, for example, after variable desired positions change.
    public void updateBlockPositions() {
        for(Block b: list) {
            b.updateWeightedPosition();
        }
    }

    // split each block across its constraint with the minimum lagrangian 
    public void split(List<Constraint> inactive) {
        this.updateBlockPositions();
        
        List<Block> toRemove = new ArrayList<Block>();
        List<Block> toAdd = new ArrayList<Block>();
        for(Block b: list) {
            Constraint v = b.findMinLM();
            if (v != null && v.lm < Solver.LAGRANGIAN_TOLERANCE) {
                b = v.left.block;
                
                toAdd.addAll(Arrays.asList(Block.split(v))); //insert(nb);
                
                toRemove.add(b);
                //remove(b);
                inactive.add(v);
            }
        }
        
        for(Block aB: toAdd) {
            insert(aB);
        }
        list.removeAll(toRemove);
    }
}

/*
    export class Blocks {
        private list: Block[];

        constructor(public vs: Variable[]) {
            var n = vs.length;
            this.list = new Array(n);
            while (n--) {
                var b = new Block(vs[n]);
                this.list[n] = b;
                b.blockInd = n;
            }
        }

        cost(): number {
            var sum = 0, i = this.list.length;
            while (i--) sum += this.list[i].cost();
            return sum;
        }

        insert(b: Block) {
            b.blockInd = this.list.length;
            this.list.push(b);
        }

        remove(b: Block) {
            var last = this.list.length - 1;
            var swapBlock = this.list[last];
            this.list.length = last;
            if (b !== swapBlock) {
                this.list[b.blockInd] = swapBlock;
                swapBlock.blockInd = b.blockInd;
            }
        }

        // merge the blocks on either side of the specified constraint, by copying the smaller block into the larger
        // and deleting the smaller.
        merge(c: Constraint): void {
            var l = c.left.block, r = c.right.block;
            
            var dist = c.right.offset - c.left.offset - c.gap;
            if (l.vars.length < r.vars.length) {
                r.mergeAcross(l, c, dist);
                this.remove(l);
            } else {
                l.mergeAcross(r, c, -dist);
                this.remove(r);
            }
        }

        forEach(f: (b: Block, i: number) => void ) {
            this.list.forEach(f);
        }
        
        // useful, for example, after variable desired positions change.
        updateBlockPositions(): void {
            this.list.forEach(b=> b.updateWeightedPosition());
        }

        // split each block across its constraint with the minimum lagrangian 
        split(inactive: Constraint[]): void {
            this.updateBlockPositions();
            this.list.forEach(b=> {
                var v = b.findMinLM();
                if (v !== null && v.lm < Solver.LAGRANGIAN_TOLERANCE) {
                    b = v.left.block;
                    Block.split(v).forEach(nb=>this.insert(nb));
                    this.remove(b);
                    inactive.push(v);
                }
            });
        }
    }
*/