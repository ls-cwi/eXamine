package org.cwi.examine.internal.layout.dwyer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.cwi.examine.internal.layout.dwyer.vpsc.Variable;

/**
 * Two dimensional rectangle with minimum and maximum coordinates.
 * Includes functionality for generating linear constraints.
 */
public class Rectangle {
    
    public double x, X, y, Y;
    
    public Rectangle(double x, double X, double y, double Y) {
        this.x = x;
        this.X = X;
        this.y = y;
        this.Y = Y;
    }

    public static Rectangle empty() {
        return new Rectangle(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    public double cx() {
        return (this.x + this.X) / 2;
    }

    public double cy(){
        return (this.y + this.Y) / 2;
    }

    public double overlapX(Rectangle r) {
        double ux = this.cx();
        double vx = r.cx();
        if (ux <= vx && r.x < this.X) return this.X - r.x;
        if (vx <= ux && this.x < r.X) return r.X - this.x;
        return 0;
    }

    public double overlapY(Rectangle r) {
        double uy = this.cy();
        double vy = r.cy();
        if (uy <= vy && r.y < this.Y) return this.Y - r.y;
        if (vy <= uy && this.y < r.Y) return r.Y - this.y;
        return 0;
    }

    public void setXCentre(double cx) {
        double dx = cx - this.cx();
        this.x += dx;
        this.X += dx;
    }

    public void setYCentre(double cy) {
        double dy = cy - this.cy();
        this.y += dy;
        this.Y += dy;
    }

    public double width() {
        return this.X - this.x;
    }

    public double height() {
        return this.Y - this.y;
    }

    public Rectangle union(Rectangle r){
        return new Rectangle(Math.min(this.x, r.x), Math.max(this.X, r.X), Math.min(this.y, r.y), Math.max(this.Y, r.Y));
    }
    
    public static Rectangle union(Iterable<Rectangle> rectangles) {
        Rectangle bounds = Rectangle.empty();
        for(Rectangle r: rectangles) {
            bounds = bounds.union(r);
        }
        return bounds;
    }

    public Point rayIntersection(double x2, double y2) {
        double x1 = this.cx();
        double y1 = this.cy();
        double[][] sides = new double[][] {
            {this.x, this.y, this.X, this.y},
            {this.X, this.y, this.X, this.Y},
            {this.X, this.Y, this.x, this.Y},
            {this.x, this.Y, this.x, this.y}
        };
        
        for (int i = 0; i < 4; ++i) {
            Point r = Rectangle.lineIntersection(x1, y1, x2, y2, sides[i][0], sides[i][1], sides[i][2], sides[i][3]);
            if (r != null) return new Point(r.x, r.y);
        }
        
        return null;
    }

    public static Point lineIntersection(
        double x1, double y1,
        double x2, double y2,
        double x3, double y3,
        double x4, double y4) {
        
        double dx12 = x2 - x1, dx34 = x4 - x3;
        double dy12 = y2 - y1, dy34 = y4 - y3;
        double denominator = dy34 * dx12 - dx34 * dy12;
        if (denominator == 0) return null;
        
        double dx31 = x1 - x3, dy31 = y1 - y3;
        double numa = dx34 * dy31 - dy34 * dx31;
        double a = numa / denominator;
        double numb = dx12 * dy31 - dy12 * dx31;
        double b = numb / denominator;
        
        if (a >= 0 && a <= 1 && b >= 0 && b <= 1) {
            return new Point(x1 + a * dx12, y1 + a * dy12);
        }
        
        return null;
    }

    public Rectangle inflate(double pad) {
        return new Rectangle(this.x - pad, this.X + pad, this.y - pad, this.Y + pad);
    }
    
    
    /**
     * Hierarchy of bounds.
     */
    public static interface Leaf {
        public Rectangle bounds();
        public Variable variable();
    }

    public static interface Group {
        public Rectangle bounds();
        public double padding();
        public Leaf[] leaves();
        public Group[] groups();
        public Variable minVar();
        public Variable maxVar();
    }
    
    public static class Link {
        public Point sourceIntersection,
                     targetIntersection,
                     arrowStart;
    }

    public static Rectangle computeGroupBounds(Group g) {
        List<Rectangle> subBounds = new ArrayList<Rectangle>();
        for(Leaf leaf: g.leaves()) {
            subBounds.add(leaf.bounds());
        }
        
        if (g.groups() != null) {
            for(Group group: g.groups()) {
                subBounds.add(group.bounds());
            }
        }
        
        return Rectangle.union(subBounds);
    }
    
    
    /**
     * Constraint derivation.
     */
    static void makeEdgeBetween(Link link, Rectangle source, Rectangle target, double ah) {
        Point si = source.rayIntersection(target.cx(), target.cy());
        if (si == null) si = new Point(source.cx(), source.cy());
        
        Point ti = target.rayIntersection(source.cx(), source.cy());
        if (ti == null) ti = new Point(target.cx(), target.cy());
        
        double dx = ti.x - si.x;
        double dy = ti.y - si.y;
        double l = Math.sqrt(dx * dx + dy * dy);
        double al = l - ah;
        
        link.sourceIntersection = si;
        link.targetIntersection = ti;
        link.arrowStart = new Point(si.x + al * dx / l, si.y + al * dy / l);
    }

    public static class Node {
        Variable v;
        Rectangle r;
        double pos;
        RBTree<Node> prev;
        RBTree<Node> next;

        public Node(Variable v, Rectangle r, double pos) {
            this.v = v;
            this.r = r;
            this.pos = pos;
            this.prev = makeRBTree();
            this.next = makeRBTree();
        }
        
    }

    public static class Event {
        boolean isOpen;
        Node v;
        double pos;
        
        public Event(boolean isOpen, Node v, double pos) {
            this.isOpen = isOpen;
            this.v = v;
            this.pos = pos;
        }
        
    }

    public static double compareEvents(Event a, Event b) {
        if (a.pos > b.pos) {
            return 1;
        }
        if (a.pos < b.pos) {
            return -1;
        }
        if (a.isOpen) {
            // open must come before close
            return -1;
        }
        return 0;
    }

    public static RBTree<Node> makeRBTree() {
        //return new RBTree<Node>((a, b) => a.pos - b.pos);
        return new RBTree<Node>(new Comparator<Node>() {

            @Override
            public int compare(Node a, Node b) {
                double d = a.pos - b.pos;
                return d == 0 ? 0 : (d < 0 ? -1 : 1);
            }
        
        });
    }
/*
    public static abstract class RectAccessors {
        public abstract double getCentre(Rectangle r);
        public abstract double getOpen(Rectangle r);
        public abstract double getClose(Rectangle r);
        public abstract double getSize(Rectangle r);
        public abstract Rectangle makeRect(double number, double close, double center, double size);
        public abstract void findNeighbours(Node v, RBTree<Node> scanline);
    }

    RectAccessors xRect = new RectAccessors() {
        @Override
        public double getCentre(Rectangle r) { return r.cx(); };
        
        @Override
        public double getOpen(Rectangle r) { return r.y; };
        
        @Override
        public double getClose(Rectangle r) { return r.Y; };
        
        @Override
        public double getSize(Rectangle r) { return r.width(); };
        
        @Override
        public Rectangle makeRect(double open, double close, double center, double size) {
            return new Rectangle(center - size / 2, center + size / 2, open, close);
        }
        
        @Override
        public void findNeighbours(Node v, RBTree<Node> scanline) {
            findXNeighbours(v, scanline);
        }
        
    };
    
    RectAccessors yRect = new RectAccessors() {
        @Override
        public double getCentre(Rectangle r) { return r.cy(); };
        
        @Override
        public double getOpen(Rectangle r) { return r.x; };
        
        @Override
        public double getClose(Rectangle r) { return r.X; };
        
        @Override
        public double getSize(Rectangle r) { return r.height(); };
        
        @Override
        public Rectangle makeRect(double open, double close, double center, double size) {
            return new Rectangle(open, close, center - size / 2, center + size / 2);
        }
        
        @Override
        public void findNeighbours(Node v, RBTree<Node> scanline) {
            findYNeighbours(v, scanline);
        }
        
    };

    public static Constraint[] generateGroupConstraints(Group root, RectAccessors f, double minSep, boolean isContained) {
        double padding = root.padding() == Double.NaN ? 1 : root.padding();
        int gn = root.groups() != null ? root.groups().length : 0;
        int ln = root.leaves() != null ? root.leaves().length : 0;
        Constraint[] childConstraints = !gn ?
                new Constraint[] :
                root.groups.reduce((ccs: Constraint[], g) => ccs.concat(generateGroupConstraints(g, f, minSep, true)), []);
        int n = (isContained ? 2 : 0) + ln + gn;
        Variable[] vs = new Variable[n];
        Rectangle[] rs = new Rectangle[n];
        int i = 0;
        int add = (r, v) => { rs[i] = r; vs[i++] = v }; // To do: make function.
            
        if (isContained) {
            Rectangle b = root.bounds();
            double c = f.getCentre(b);
            double s = f.getSize(b) / 2;
            double open = f.getOpen(b);
            double close = f.getClose(b);
            double min = c - s, max = c + s;
            root.minVar().desiredPosition = min;
            add(f.makeRect(open, close, min, padding), root.minVar());
            root.maxVar().desiredPosition = max;
            add(f.makeRect(open, close, max, padding), root.maxVar());
        }
        if (ln) root.leaves.forEach(l => add(l.bounds, l.variable));
        if (gn) root.groups.forEach(g => {
            var b: Rectangle = g.bounds;
            add(f.makeRect(f.getOpen(b), f.getClose(b), f.getCentre(b), f.getSize(b)), g.minVar);
        });
        var cs = generateConstraints(rs, vs, f, minSep);
        if (gn) {
            vs.forEach(v => { v.cOut = [], v.cIn = [] });
            cs.forEach(c => { c.left.cOut.push(c), c.right.cIn.push(c) });
            root.groups.forEach(g => {
                var gapAdjustment = (padding - f.getSize(g.bounds)) / 2;
                g.minVar.cIn.forEach(c => c.gap += gapAdjustment);
                g.minVar.cOut.forEach(c => { c.left = g.maxVar; c.gap += gapAdjustment; });
            });
        }
        return childConstraints.concat(cs);
    }

    function generateConstraints(rs: Rectangle[], vars: Variable[],
        rect: RectAccessors, minSep: number): Constraint[]
    {
        var i, n = rs.length;
        var N = 2 * n;
        console.assert(vars.length >= n);
        var events = new Array<Event>(N);
        for (i = 0; i < n; ++i) {
            var r = rs[i];
            var v = new Node(vars[i], r, rect.getCentre(r));
            events[i] = new Event(true, v, rect.getOpen(r));
            events[i + n] = new Event(false, v, rect.getClose(r));
        }
        events.sort(compareEvents);
        var cs = new Array<Constraint>();
        var scanline = makeRBTree();
        for (i = 0; i < N; ++i) {
            var e = events[i];
            var v = e.v;
            if (e.isOpen) {
                scanline.insert(v);
                rect.findNeighbours(v, scanline);
            } else {
                // close event
                scanline.remove(v);
                var makeConstraint = (l, r) => {
                    var sep = (rect.getSize(l.r) + rect.getSize(r.r)) / 2 + minSep;
                    cs.push(new Constraint(l.v, r.v, sep));
                };
                var visitNeighbours = (forward, reverse, mkcon) => {
                    var u, it = v[forward].iterator();
                    while ((u = it[forward]()) !== null) {
                        mkcon(u, v);
                        u[reverse].remove(v);
                    }
                };
                visitNeighbours("prev", "next", (u, v) => makeConstraint(u, v));
                visitNeighbours("next", "prev", (u, v) => makeConstraint(v, u));
            }
        }
        console.assert(scanline.size === 0);
        return cs;
    }

    public static void findXNeighbours(Node v, RBTree<Node> scanline){
        var f = (forward, reverse) => {
            var it = scanline.findIter(v);
            var u;
            while ((u = it[forward]()) !== null) {
                var uovervX = u.r.overlapX(v.r);
                if (uovervX <= 0 || uovervX <= u.r.overlapY(v.r)) {
                    v[forward].insert(u);
                    u[reverse].insert(v);
                }
                if (uovervX <= 0) {
                    break;
                }
            }
        }
        f("next", "prev");
        f("prev", "next");
    }

    function findYNeighbours(v: Node, scanline: RBTree<Node>): void {
        var f = (forward, reverse) => {
            var u = scanline.findIter(v)[forward]();
            if (u !== null && u.r.overlapX(v.r) > 0) {
                v[forward].insert(u);
                u[reverse].insert(v);
            }
        }
        f("next", "prev");
        f("prev", "next");
    }

    export function generateXConstraints(rs: Rectangle[], vars: Variable[]): Constraint[] {
        return generateConstraints(rs, vars, xRect, 1e-6);
    }

    export function generateYConstraints(rs: Rectangle[], vars: Variable[]): Constraint[] {
        return generateConstraints(rs, vars, yRect, 1e-6);
    }

    export function generateXGroupConstraints(root: Group): Constraint[] {
        return generateGroupConstraints(root, xRect, 1e-6);
    }

    export function generateYGroupConstraints(root: Group): Constraint[] {
        return generateGroupConstraints(root, yRect, 1e-6);
    }

    export function removeOverlaps(rs: Rectangle[]): void {
        var vs = rs.map(r => new vpsc.Variable(r.cx()));
        var cs = vpsc.generateXConstraints(rs, vs);
        var solver = new vpsc.Solver(vs, cs);
        solver.solve();
        vs.forEach((v, i) => rs[i].setXCentre(v.position()));
        vs = rs.map(function (r) {
            return new vpsc.Variable(r.cy());
        });
        cs = vpsc.generateYConstraints(rs, vs);
        solver = new vpsc.Solver(vs, cs);
        solver.solve();
        vs.forEach((v, i) => rs[i].setYCentre(v.position()));
    }

    export interface GraphNode extends Leaf {
        fixed: boolean;
        width: number;
        height: number;
        x: number;
        y: number;
        px: number;
        py: number;
    }

    class IndexedVariable extends Variable {
        constructor(public index: number, w: number) {
            super(0, w);
        }
    }

    export class Projection {
        private xConstraints: Constraint[];
        private yConstraints: Constraint[];
        private variables: Variable[];

        constructor(private nodes: GraphNode[],
            private groups: Group[],
            private rootGroup: Group = null,
            constraints: any[]= null,
            private avoidOverlaps: boolean = false)
        {
            this.variables = nodes.map((v, i) => {
                return v.variable = new IndexedVariable(i, 1);
            });

            if (constraints) this.createConstraints(constraints);

            if (avoidOverlaps && rootGroup && typeof rootGroup.groups !== 'undefined') {
                nodes.forEach(v => {
                    var w2 = v.width / 2, h2 = v.height / 2;
                    v.bounds = new vpsc.Rectangle(v.x - w2, v.x + w2, v.y - h2, v.y + h2);
                });
                computeGroupBounds(rootGroup);
                var i = nodes.length;
                groups.forEach(g => {
                    this.variables[i] = g.minVar = new IndexedVariable(i++, 0.01);
                    this.variables[i] = g.maxVar = new IndexedVariable(i++, 0.01);
                });
            }
        }

        private createSeparation(c: any) : Constraint {
            return new Constraint(
                this.nodes[c.left].variable,
                this.nodes[c.right].variable,
                c.gap,
                typeof c.equality !== "undefined" ? c.equality : false);
        }

        private makeFeasible(c: any) {
            if (!this.avoidOverlaps) return;
            var axis = 'x', dim = 'width';
            if (c.axis === 'x') axis = 'y', dim = 'height';
            var vs: GraphNode[] = c.offsets.map(o => this.nodes[o.node]).sort((a, b) => a[axis] - b[axis]);
            var p: GraphNode = null;
            vs.forEach(v => {
                if (p) v[axis] = p[axis] + p[dim] + 1
                p = v;
            });
        }

        private createAlignment(c: any) {
            var u = this.nodes[c.offsets[0].node].variable;
            this.makeFeasible(c);
            var cs = c.axis === 'x' ? this.xConstraints : this.yConstraints;
            c.offsets.slice(1).forEach(o => {
                var v = this.nodes[o.node].variable;
                cs.push(new Constraint(u, v, o.offset, true));
            });
        }

        private createConstraints(constraints: any[]) {
            var isSep = c => typeof c.type === 'undefined' || c.type === 'separation';
            this.xConstraints = constraints
                .filter(c => c.axis === "x" && isSep(c))
                .map(c => this.createSeparation(c));
            this.yConstraints = constraints
                .filter(c => c.axis === "y" && isSep(c))
                .map(c => this.createSeparation(c));
            constraints
                .filter(c => c.type === 'alignment')
                .forEach(c => this.createAlignment(c));
        }

        private setupVariablesAndBounds(x0: number[], y0: number[], desired: number[], getDesired: (v:GraphNode) => number) {
            this.nodes.forEach((v, i) => {
                if (v.fixed) {
                    v.variable.weight = 1000;
                    desired[i] = getDesired(v);
                } else {
                    v.variable.weight = 1;
                }
                var w = v.width / 2, h = v.height / 2;
                var ix = x0[i], iy = y0[i];
                v.bounds = new Rectangle(ix - w, ix + w, iy - h, iy + h);
            });
        }

        xProject(x0: number[], y0: number[], x: number[]) {
            if (!this.rootGroup && !(this.avoidOverlaps || this.xConstraints)) return;
            this.project(x0, y0, x0, x, v=> v.px, this.xConstraints, generateXGroupConstraints,
                v => v.bounds.setXCentre(x[(<IndexedVariable>v.variable).index] = v.variable.position()),
                g => {
                    g.bounds.x = x[(<IndexedVariable>g.minVar).index] = g.minVar.position();
                    g.bounds.X = x[(<IndexedVariable>g.maxVar).index] = g.maxVar.position();
                });
        }

        yProject(x0: number[], y0: number[], y: number[]) {
            if (!this.rootGroup && !this.yConstraints) return;
            this.project(x0, y0, y0, y, v=> v.py, this.yConstraints, generateYGroupConstraints,
                v => v.bounds.setYCentre(y[(<IndexedVariable>v.variable).index] = v.variable.position()),
                g => {
                    g.bounds.y = y[(<IndexedVariable>g.minVar).index] = g.minVar.position();
                    g.bounds.Y = y[(<IndexedVariable>g.maxVar).index] = g.maxVar.position();
                });
        }

        projectFunctions(): { (x0: number[], y0: number[], r: number[]): void }[]{
            return [
                (x0, y0, x) => this.xProject(x0, y0, x),
                (x0, y0, y) => this.yProject(x0, y0, y)
            ];
        }

        private project(x0: number[], y0: number[], start: number[], desired: number[], 
            getDesired: (v: GraphNode) => number,
            cs: Constraint[], 
            generateConstraints: (g: Group) => Constraint[], 
            updateNodeBounds: (v: GraphNode) => any,
            updateGroupBounds: (g: Group) => any)
        {
            this.setupVariablesAndBounds(x0, y0, desired, getDesired);
            if (this.rootGroup && this.avoidOverlaps) {
                computeGroupBounds(this.rootGroup);
                cs = cs.concat(generateConstraints(this.rootGroup));
            }
            this.solve(this.variables, cs, start, desired);
            this.nodes.forEach(updateNodeBounds);
            if (this.rootGroup && this.avoidOverlaps) {
                this.groups.forEach(updateGroupBounds);
            }
        }

        private solve(vs: Variable[], cs: Constraint[], starting: number[], desired: number[]) {
            var solver = new vpsc.Solver(vs, cs);
            solver.setStartingPositions(starting);
            solver.setDesiredPositions(desired);
            solver.solve();
        }
    }
        */
}

/*module vpsc {
    export interface Leaf {
        bounds: Rectangle;
        variable: Variable;
    }

    export interface Group {
        bounds: Rectangle;
        padding: number;
        leaves: Leaf[];
        groups: Group[];
        minVar: Variable;
        maxVar: Variable;
    }

    export function computeGroupBounds(g: Group): Rectangle {
        g.bounds = g.leaves.reduce((r: Rectangle, c) => c.bounds.union(r), Rectangle.empty());
        if (typeof g.groups !== "undefined")
            g.bounds = <Rectangle>g.groups.reduce((r: Rectangle, c) => computeGroupBounds(c).union(r), g.bounds);
        return g.bounds;
    }

    export class Rectangle {
        constructor(
            public x: number,
            public X: number,
            public y: number,
            public Y: number) { }

        static empty(): Rectangle { return new Rectangle(Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY, Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY); }

        cx(): number { return (this.x + this.X) / 2; }

        cy(): number { return (this.y + this.Y) / 2; }

        overlapX(r: Rectangle): number {
            var ux = this.cx(), vx = r.cx();
            if (ux <= vx && r.x < this.X) return this.X - r.x;
            if (vx <= ux && this.x < r.X) return r.X - this.x;
            return 0;
        }

        overlapY(r: Rectangle): number {
            var uy = this.cy(), vy = r.cy();
            if (uy <= vy && r.y < this.Y) return this.Y - r.y;
            if (vy <= uy && this.y < r.Y) return r.Y - this.y;
            return 0;
        }

        setXCentre(cx: number): void {
            var dx = cx - this.cx();
            this.x += dx;
            this.X += dx;
        }

        setYCentre(cy: number): void {
            var dy = cy - this.cy();
            this.y += dy;
            this.Y += dy;
        }

        width(): number {
            return this.X - this.x;
        }

        height(): number {
            return this.Y - this.y;
        }

        union(r: Rectangle): Rectangle {
            return new Rectangle(Math.min(this.x, r.x), Math.max(this.X, r.X), Math.min(this.y, r.y), Math.max(this.Y, r.Y));
        }

        rayIntersection(x2: number, y2: number): {
            x: number; y: number
        } {
            var x1 = this.cx(), y1 = this.cy(),
                sides = [[this.x, this.y, this.X, this.y],
                    [this.X, this.y, this.X, this.Y],
                    [this.X, this.Y, this.x, this.Y],
                    [this.x, this.Y, this.x, this.y]];
            for (var i = 0; i < 4; ++i) {
                var r = Rectangle.lineIntersection(x1, y1, x2, y2, sides[i][0], sides[i][1], sides[i][2], sides[i][3]);
                if (r !== null) return { x: r.x, y: r.y };
            }
            return null;
        }

        static lineIntersection(
            x1: number, y1: number,
            x2: number, y2: number,
            x3: number, y3: number,
            x4: number, y4: number): { x: number; y: number } {
            var dx12 = x2 - x1, dx34 = x4 - x3,
                dy12 = y2 - y1, dy34 = y4 - y3,
                denominator = dy34 * dx12 - dx34 * dy12;
            if (denominator == 0) return null;
            var dx31 = x1 - x3, dy31 = y1 - y3,
                numa = dx34 * dy31 - dy34 * dx31,
                a = numa / denominator,
                numb = dx12 * dy31 - dy12 * dx31,
                b = numb / denominator;
            if (a >= 0 && a <= 1 && b >= 0 && b <= 1) {
                return {
                    x: x1 + a * dx12,
                    y: y1 + a * dy12
                };
            }
            return null;
        }

        inflate(pad: number): Rectangle {
            return new Rectangle(this.x - pad, this.X + pad, this.y - pad, this.Y + pad);
        }
    }

    export function makeEdgeBetween(link: any, source: Rectangle, target: Rectangle, ah: number) {
        var si = source.rayIntersection(target.cx(), target.cy());
        if (!si) si = { x: source.cx(), y: source.cy() };
        var ti = target.rayIntersection(source.cx(), source.cy());
        if (!ti) ti = { x: target.cx(), y: target.cy() };
        var dx = ti.x - si.x,
            dy = ti.y - si.y,
            l = Math.sqrt(dx * dx + dy * dy), al = l - ah;
        link.sourceIntersection = si;
        link.targetIntersection = ti;
        link.arrowStart = { x: si.x + al * dx / l, y: si.y + al * dy / l };
    }

    class Node {
        prev: RBTree<Node>;
        next: RBTree<Node>;

        constructor(public v: Variable, public r: Rectangle, public pos: number) {
            this.prev = makeRBTree();
            this.next = makeRBTree();
        }
    }

    class Event {
        constructor(public isOpen: boolean, public v: Node, public pos: number) {}
    }

    function compareEvents(a: Event, b: Event): number {
        if (a.pos > b.pos) {
            return 1;
        }
        if (a.pos < b.pos) {
            return -1;
        }
        if (a.isOpen) {
            // open must come before close
            return -1;
        }
        return 0;
    }

    function makeRBTree(): RBTree<Node> {
        return new RBTree<Node>((a, b) => a.pos - b.pos);
    }

    interface RectAccessors {
        getCentre: (r: Rectangle) => number;
        getOpen: (r: Rectangle) => number;
        getClose: (r: Rectangle) => number;
        getSize: (r: Rectangle) => number;
        makeRect: (open: number, close: number, center: number, size: number) => Rectangle;
        findNeighbours: (v: Node, scanline: RBTree<Node>) => void;
    }

    var xRect: RectAccessors = {
        getCentre: r=> r.cx(),
        getOpen: r=> r.y,
        getClose: r=> r.Y,
        getSize: r=> r.width(),
        makeRect: (open, close, center, size) => new Rectangle(center - size / 2, center + size / 2, open, close) ,
        findNeighbours: findXNeighbours
    };

    var yRect: RectAccessors = {
        getCentre: r=> r.cy(),
        getOpen: r=> r.x,
        getClose: r=> r.X,
        getSize: r=> r.height(),
        makeRect: (open, close, center, size) => new Rectangle(open, close, center - size / 2, center + size / 2),
        findNeighbours: findYNeighbours
    };

    function generateGroupConstraints(root: Group, f: RectAccessors, minSep: number, isContained: boolean = false): Constraint[]
    {
        var padding = typeof root.padding === 'undefined' ? 1 : root.padding,
            gn = typeof root.groups !== 'undefined' ? root.groups.length : 0,
            ln = typeof root.leaves !== 'undefined' ? root.leaves.length : 0,
            childConstraints: Constraint[] = !gn ? []
            : root.groups.reduce((ccs: Constraint[], g) => ccs.concat(generateGroupConstraints(g, f, minSep, true)), []),
            n = (isContained ? 2 : 0) + ln + gn,
            vs: Variable[] = new Array(n),
            rs: Rectangle[] = new Array(n),
            i = 0,
            add = (r, v) => { rs[i] = r; vs[i++] = v };
        if (isContained) {
            var b: Rectangle = root.bounds,
                c = f.getCentre(b), s = f.getSize(b) / 2,
                open = f.getOpen(b), close = f.getClose(b),
                min = c - s, max = c + s;
            root.minVar.desiredPosition = min;
            add(f.makeRect(open, close, min, padding), root.minVar);
            root.maxVar.desiredPosition = max;
            add(f.makeRect(open, close, max, padding), root.maxVar);
        }
        if (ln) root.leaves.forEach(l => add(l.bounds, l.variable));
        if (gn) root.groups.forEach(g => {
            var b: Rectangle = g.bounds;
            add(f.makeRect(f.getOpen(b), f.getClose(b), f.getCentre(b), f.getSize(b)), g.minVar);
        });
        var cs = generateConstraints(rs, vs, f, minSep);
        if (gn) {
            vs.forEach(v => { v.cOut = [], v.cIn = [] });
            cs.forEach(c => { c.left.cOut.push(c), c.right.cIn.push(c) });
            root.groups.forEach(g => {
                var gapAdjustment = (padding - f.getSize(g.bounds)) / 2;
                g.minVar.cIn.forEach(c => c.gap += gapAdjustment);
                g.minVar.cOut.forEach(c => { c.left = g.maxVar; c.gap += gapAdjustment; });
            });
        }
        return childConstraints.concat(cs);
    }

    function generateConstraints(rs: Rectangle[], vars: Variable[],
        rect: RectAccessors, minSep: number): Constraint[]
    {
        var i, n = rs.length;
        var N = 2 * n;
        console.assert(vars.length >= n);
        var events = new Array<Event>(N);
        for (i = 0; i < n; ++i) {
            var r = rs[i];
            var v = new Node(vars[i], r, rect.getCentre(r));
            events[i] = new Event(true, v, rect.getOpen(r));
            events[i + n] = new Event(false, v, rect.getClose(r));
        }
        events.sort(compareEvents);
        var cs = new Array<Constraint>();
        var scanline = makeRBTree();
        for (i = 0; i < N; ++i) {
            var e = events[i];
            var v = e.v;
            if (e.isOpen) {
                scanline.insert(v);
                rect.findNeighbours(v, scanline);
            } else {
                // close event
                scanline.remove(v);
                var makeConstraint = (l, r) => {
                    var sep = (rect.getSize(l.r) + rect.getSize(r.r)) / 2 + minSep;
                    cs.push(new Constraint(l.v, r.v, sep));
                };
                var visitNeighbours = (forward, reverse, mkcon) => {
                    var u, it = v[forward].iterator();
                    while ((u = it[forward]()) !== null) {
                        mkcon(u, v);
                        u[reverse].remove(v);
                    }
                };
                visitNeighbours("prev", "next", (u, v) => makeConstraint(u, v));
                visitNeighbours("next", "prev", (u, v) => makeConstraint(v, u));
            }
        }
        console.assert(scanline.size === 0);
        return cs;
    }

    function findXNeighbours(v: Node, scanline: RBTree<Node>): void {
        var f = (forward, reverse) => {
            var it = scanline.findIter(v);
            var u;
            while ((u = it[forward]()) !== null) {
                var uovervX = u.r.overlapX(v.r);
                if (uovervX <= 0 || uovervX <= u.r.overlapY(v.r)) {
                    v[forward].insert(u);
                    u[reverse].insert(v);
                }
                if (uovervX <= 0) {
                    break;
                }
            }
        }
        f("next", "prev");
        f("prev", "next");
    }

    function findYNeighbours(v: Node, scanline: RBTree<Node>): void {
        var f = (forward, reverse) => {
            var u = scanline.findIter(v)[forward]();
            if (u !== null && u.r.overlapX(v.r) > 0) {
                v[forward].insert(u);
                u[reverse].insert(v);
            }
        }
        f("next", "prev");
        f("prev", "next");
    }

    export function generateXConstraints(rs: Rectangle[], vars: Variable[]): Constraint[] {
        return generateConstraints(rs, vars, xRect, 1e-6);
    }

    export function generateYConstraints(rs: Rectangle[], vars: Variable[]): Constraint[] {
        return generateConstraints(rs, vars, yRect, 1e-6);
    }

    export function generateXGroupConstraints(root: Group): Constraint[] {
        return generateGroupConstraints(root, xRect, 1e-6);
    }

    export function generateYGroupConstraints(root: Group): Constraint[] {
        return generateGroupConstraints(root, yRect, 1e-6);
    }

    export function removeOverlaps(rs: Rectangle[]): void {
        var vs = rs.map(r => new vpsc.Variable(r.cx()));
        var cs = vpsc.generateXConstraints(rs, vs);
        var solver = new vpsc.Solver(vs, cs);
        solver.solve();
        vs.forEach((v, i) => rs[i].setXCentre(v.position()));
        vs = rs.map(function (r) {
            return new vpsc.Variable(r.cy());
        });
        cs = vpsc.generateYConstraints(rs, vs);
        solver = new vpsc.Solver(vs, cs);
        solver.solve();
        vs.forEach((v, i) => rs[i].setYCentre(v.position()));
    }

    export interface GraphNode extends Leaf {
        fixed: boolean;
        width: number;
        height: number;
        x: number;
        y: number;
        px: number;
        py: number;
    }

    class IndexedVariable extends Variable {
        constructor(public index: number, w: number) {
            super(0, w);
        }
    }

    export class Projection {
        private xConstraints: Constraint[];
        private yConstraints: Constraint[];
        private variables: Variable[];

        constructor(private nodes: GraphNode[],
            private groups: Group[],
            private rootGroup: Group = null,
            constraints: any[]= null,
            private avoidOverlaps: boolean = false)
        {
            this.variables = nodes.map((v, i) => {
                return v.variable = new IndexedVariable(i, 1);
            });

            if (constraints) this.createConstraints(constraints);

            if (avoidOverlaps && rootGroup && typeof rootGroup.groups !== 'undefined') {
                nodes.forEach(v => {
                    var w2 = v.width / 2, h2 = v.height / 2;
                    v.bounds = new vpsc.Rectangle(v.x - w2, v.x + w2, v.y - h2, v.y + h2);
                });
                computeGroupBounds(rootGroup);
                var i = nodes.length;
                groups.forEach(g => {
                    this.variables[i] = g.minVar = new IndexedVariable(i++, 0.01);
                    this.variables[i] = g.maxVar = new IndexedVariable(i++, 0.01);
                });
            }
        }

        private createSeparation(c: any) : Constraint {
            return new Constraint(
                this.nodes[c.left].variable,
                this.nodes[c.right].variable,
                c.gap,
                typeof c.equality !== "undefined" ? c.equality : false);
        }

        private makeFeasible(c: any) {
            if (!this.avoidOverlaps) return;
            var axis = 'x', dim = 'width';
            if (c.axis === 'x') axis = 'y', dim = 'height';
            var vs: GraphNode[] = c.offsets.map(o => this.nodes[o.node]).sort((a, b) => a[axis] - b[axis]);
            var p: GraphNode = null;
            vs.forEach(v => {
                if (p) v[axis] = p[axis] + p[dim] + 1
                p = v;
            });
        }

        private createAlignment(c: any) {
            var u = this.nodes[c.offsets[0].node].variable;
            this.makeFeasible(c);
            var cs = c.axis === 'x' ? this.xConstraints : this.yConstraints;
            c.offsets.slice(1).forEach(o => {
                var v = this.nodes[o.node].variable;
                cs.push(new Constraint(u, v, o.offset, true));
            });
        }

        private createConstraints(constraints: any[]) {
            var isSep = c => typeof c.type === 'undefined' || c.type === 'separation';
            this.xConstraints = constraints
                .filter(c => c.axis === "x" && isSep(c))
                .map(c => this.createSeparation(c));
            this.yConstraints = constraints
                .filter(c => c.axis === "y" && isSep(c))
                .map(c => this.createSeparation(c));
            constraints
                .filter(c => c.type === 'alignment')
                .forEach(c => this.createAlignment(c));
        }

        private setupVariablesAndBounds(x0: number[], y0: number[], desired: number[], getDesired: (v:GraphNode) => number) {
            this.nodes.forEach((v, i) => {
                if (v.fixed) {
                    v.variable.weight = 1000;
                    desired[i] = getDesired(v);
                } else {
                    v.variable.weight = 1;
                }
                var w = v.width / 2, h = v.height / 2;
                var ix = x0[i], iy = y0[i];
                v.bounds = new Rectangle(ix - w, ix + w, iy - h, iy + h);
            });
        }

        xProject(x0: number[], y0: number[], x: number[]) {
            if (!this.rootGroup && !(this.avoidOverlaps || this.xConstraints)) return;
            this.project(x0, y0, x0, x, v=> v.px, this.xConstraints, generateXGroupConstraints,
                v => v.bounds.setXCentre(x[(<IndexedVariable>v.variable).index] = v.variable.position()),
                g => {
                    g.bounds.x = x[(<IndexedVariable>g.minVar).index] = g.minVar.position();
                    g.bounds.X = x[(<IndexedVariable>g.maxVar).index] = g.maxVar.position();
                });
        }

        yProject(x0: number[], y0: number[], y: number[]) {
            if (!this.rootGroup && !this.yConstraints) return;
            this.project(x0, y0, y0, y, v=> v.py, this.yConstraints, generateYGroupConstraints,
                v => v.bounds.setYCentre(y[(<IndexedVariable>v.variable).index] = v.variable.position()),
                g => {
                    g.bounds.y = y[(<IndexedVariable>g.minVar).index] = g.minVar.position();
                    g.bounds.Y = y[(<IndexedVariable>g.maxVar).index] = g.maxVar.position();
                });
        }

        projectFunctions(): { (x0: number[], y0: number[], r: number[]): void }[]{
            return [
                (x0, y0, x) => this.xProject(x0, y0, x),
                (x0, y0, y) => this.yProject(x0, y0, y)
            ];
        }

        private project(x0: number[], y0: number[], start: number[], desired: number[], 
            getDesired: (v: GraphNode) => number,
            cs: Constraint[], 
            generateConstraints: (g: Group) => Constraint[], 
            updateNodeBounds: (v: GraphNode) => any,
            updateGroupBounds: (g: Group) => any)
        {
            this.setupVariablesAndBounds(x0, y0, desired, getDesired);
            if (this.rootGroup && this.avoidOverlaps) {
                computeGroupBounds(this.rootGroup);
                cs = cs.concat(generateConstraints(this.rootGroup));
            }
            this.solve(this.variables, cs, start, desired);
            this.nodes.forEach(updateNodeBounds);
            if (this.rootGroup && this.avoidOverlaps) {
                this.groups.forEach(updateGroupBounds);
            }
        }

        private solve(vs: Variable[], cs: Constraint[], starting: number[], desired: number[]) {
            var solver = new vpsc.Solver(vs, cs);
            solver.setStartingPositions(starting);
            solver.setDesiredPositions(desired);
            solver.solve();
        }
    }
}*/