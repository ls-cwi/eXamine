package org.cwi.examine.internal.layout.dwyer.cola;

import java.awt.Point;

/**
 * Uses a gradient descent approach to reduce a stress or p-stress
 * goal function over a graph with specified ideal edge lengths or
 * a square matrix of dissimilarities.
 */
public class Descent {
    public static final double threshold = 0.001;   //0.00001;
    
    public double[][][] H;  // Hessian matrix.
    public double[][] g;    // Gradient vector.
    public double x[][];    // Positions vector.
    public int k;           // Dimensionality.
    public int n;           // Number of data-points / nodes / size of vectors/matrices.
    public Locks locks;     // Position locks.

    private double minD;

    // Pool of arrays of size n used internally, allocated in constructor.
    private final double[][] Hd;
    private final double[][] a;
    private final double[][] b;
    private final double[][] c;
    private final double[][] d;
    private final double[][] e;
    private final double[][] ia;
    private final double[][] ib;
    
    public double[][] D, G;

    private final PseudoRandom random = new PseudoRandom();

    public Projection[] project;
    
    public static abstract class Projection {
        public abstract void apply(double[] x0, double[] y0, double[] r);
    }

    /**
     * @param x {number[][]} initial coordinates for nodes
     * @param D {number[][]} matrix of desired distances between pairs of nodes
     * @param G {number[][]} [default=null] if specified, G is a matrix of weights for goal terms between pairs of nodes.  
     * If G[i][j] > 1 and the separation between nodes i and j is greater than their ideal distance, then there is no contribution for this pair to the goal
     * If G[i][j] <= 1 then it is used as a weighting on the contribution of the variance between ideal and actual separation between i and j to the goal function
     */
    public Descent(double[][] x, double[][] D, double[][] G) {
        this.x = x;
        this.D = D;
        this.G = G;
        
        this.k = x.length;    // dimensionality
        this.n = x[0].length; // number of nodes
        this.H = new double[k][n][n];
        this.g = new double[k][n];
        this.Hd = new double[k][n];
        this.a = new double[k][n];
        this.b = new double[k][n];
        this.c = new double[k][n];
        this.d = new double[k][n];
        this.e = new double[k][n];
        this.ia = new double[k][n];
        this.ib = new double[k][n];
        this.locks = new Locks();
        this.minD = Double.MAX_VALUE;
        
        for(int i = n - 1; i >=0; i--) {
            int j = n;
            while (--j > i) {
                double lD = D[i][j];
                if (lD > 0 && lD < this.minD) {
                    this.minD = lD;
                }
            }
        }
        
        if (this.minD == Double.MAX_VALUE) this.minD = 1;
    }

    public static double[][] createSquareMatrix(int n, MatrixFillFunction f) {
        double[][] M = new double[n][n];
        
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                M[i][j] = f.apply(i, j);
            }
        }
        
        return M;
    }
    
    public static interface MatrixFillFunction {
        public double apply(int i, int j);
    }

    private double[] offsetDir() {
        double[] u = new double[this.k];
        
        double l = 0;
        for (int i = 0; i < this.k; ++i) {
            double r = this.random.getNextBetween(0.01, 1) - 0.5;
            u[i] = r;
            l += r * r;
        }
        l = Math.sqrt(l);
        
        for (int i = 0; i < this.k; ++i) {
            u[i] *= this.minD / l;
        }
        
        return u;
    }

    public void computeDerivatives(final double[][] x) {
        if (n <= 1) return;
        int i;
        
        double[] d1 = new double[k];
        double[] d2 = new double[k];
        double[] Huu = new double[k];
        double maxH = 0;
        
        for (int u = 0; u < n; ++u) {
            for (i = 0; i < k; ++i) Huu[i] = g[i][u] = 0;
            
            for (int v = 0; v < n; ++v) {
                if (u == v) continue;
                double sd2;
                while (true) {
                    sd2 = 0;
                    for (i = 0; i < k; ++i) {
                        double dx = d1[i] = x[i][u] - x[i][v];
                        sd2 += d2[i] = dx * dx;
                    }
                    if (sd2 > 1e-9) break;
                    double[] rd = offsetDir();
                    for (i = 0; i < k; ++i) x[i][v] += rd[i];
                }
                
                double l = Math.sqrt(sd2);
                double lD = D[u][v];
                double weight = G != null ? G[u][v] : 1;
                if (weight > 1 && l > lD || Double.isInfinite(lD)) {
                    for (i = 0; i < k; ++i) H[i][u][v] = 0;
                    continue;
                }
                if (weight > 1) {
                    weight = 1;
                }
                
                double D2 = lD * lD;
                double gs = weight * (l - lD) / (D2 * l);
                double hs = -weight / (D2 * l * l * l);
                for (i = 0; i < k; ++i) {
                    g[i][u] += d1[i] * gs;
                    Huu[i] -= H[i][u][v] = hs * (lD * (d2[i] - sd2) + l * sd2);
                }
            }
            for (i = 0; i < k; ++i) maxH = Math.max(maxH, H[i][u][u] = Huu[i]);
        }
        
        if (!this.locks.isEmpty()) {
            final double fMaxH = maxH;
            
            this.locks.apply(new Locks.LockOperation() {

                @Override
                public void apply(int u, Point p) {
                    // Assume two-dimensional points for now.
                    H[0][u][u] += fMaxH;
                    g[0][u] -= fMaxH * (p.x - x[0][u]);
                    H[1][u][u] += fMaxH;
                    g[1][u] -= fMaxH * (p.y - x[1][u]);
                }
                
            });
        }
    }

    private static double dotProd(double[] a, double[] b) {
        double x = 0;
        
        for (int i = 0; i < a.length; i++) x += a[i] * b[i];
        
        return x;
    }

    // result r = matrix m * vector v
    private static void rightMultiply(double [][] m, double[] v, double[] r) {
        for(int i = 0; i < m.length; i++) r[i] = Descent.dotProd(m[i], v);
    }

    public double computeStepSize(double[][] d) {
        double numerator = 0;
        double denominator = 0;
        
        for (int i = 0; i < 2; ++i) {
            numerator += Descent.dotProd(g[i], d[i]);
            Descent.rightMultiply(H[i], d[i], Hd[i]);
            denominator += Descent.dotProd(d[i], Hd[i]);
        }
        
        if (denominator == 0 || Double.isInfinite(denominator)) return 0;
        
        return numerator / denominator;
    }

    public double reduceStress() {
        computeDerivatives(x);
        
        double alpha = computeStepSize(g);
        for (int i = 0; i < k; ++i) {
            this.takeDescentStep(x[i], g[i], alpha);
        }
        
        return computeStress();
    }

    private static void copy(double[][] a, double[][] b) {
        int m = a.length;
        int n = b[0].length;
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                b[i][j] = a[i][j];
            }
        }
    }

    private void stepAndProject(double[][] x0, double[][] r, double[][] d, double stepSize) {
        Descent.copy(x0, r);
        
        takeDescentStep(r[0], d[0], stepSize);
        if (project != null) project[0].apply(x0[0], x0[1], r[0]);
        
        takeDescentStep(r[1], d[1], stepSize);
        if (project != null)  project[1].apply(r[0], x0[1], r[1]);
    }

    private static void mApply(int m, int n, MatrixApplyFunction f) {
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                f.apply(i, j);
            }
        }
    }
    
    private void matrixApply(MatrixApplyFunction f) {
        Descent.mApply(k, n, f);
    }
    
    public static interface MatrixApplyFunction {
        public void apply(int i, int j);
    }

    private void computeNextPosition(final double[][] x0, final double[][] r) {
        computeDerivatives(x0);
        double alpha = computeStepSize(g);
        stepAndProject(x0, r, g, alpha);

        if (project != null) {
            matrixApply(new MatrixApplyFunction() {

                @Override
                public void apply(int i, int j) {
                    e[i][j] = x0[i][j] - r[i][j];
                }
                
            });
            
            double beta = computeStepSize(e);
            beta = Math.max(0.2, Math.min(beta, 1));
            stepAndProject(x0, r, e, beta);
        }
    }

    public boolean run(int iterations) {
        double stress = Double.POSITIVE_INFINITY;
        boolean converged = false;
        
        while (!converged && iterations-- > 0) {
            double s = rungeKutta();
            converged = Math.abs(stress / s - 1) < threshold;
            stress = s;
        }
        
        return converged;
    }

    public double rungeKutta() {
        computeNextPosition(x, a);
        Descent.mid(x, a, ia);
        computeNextPosition(ia, b);
        Descent.mid(x, b, ib);
        computeNextPosition(ib, c);
        computeNextPosition(c, d);
        
        matrixApply(new MatrixApplyFunction() {

            @Override
            public void apply(int i, int j) {
                x[i][j] = (a[i][j] + 2.0 * b[i][j] + 2.0 * c[i][j] + d[i][j]) / 6.0;
            }
            
        });
        
        return computeStress();
    }

    private static void mid(final double[][] a, final double[][] b, final double[][] m) {
        Descent.mApply(a.length, a[0].length, new MatrixApplyFunction() {

            @Override
            public void apply(int i, int j) {
                m[i][j] = a[i][j] + (b[i][j] - a[i][j]) / 2.0;
            }
            
        });
    }

    public void takeDescentStep(double[] x, double[] d, double stepSize) {
        for (int i = 0; i < n; ++i) {
            x[i] = x[i] - stepSize * d[i];
        }
    }

    public double computeStress() {
        double stress = 0;
        
        for (int u = 0; u < n - 1; ++u) {
            for (int v = u + 1; v < n; ++v) {
                double l = 0;
                for (int i = 0; i < k; ++i) {
                    double dx = x[i][u] - x[i][v];
                    l += dx * dx;
                }
                l = Math.sqrt(l);
                
                double dd = D[u][v];
                if (Double.isInfinite(dd)) continue;
                
                double rl = dd - l;
                stress += rl * rl / (dd * dd);
            }
        }
        
        return stress;
    }
    
}