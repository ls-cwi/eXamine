package org.cytoscape.examine.internal.signal.gui;

import org.cytoscape.examine.internal.graphics.StaticGraphics;
import org.cytoscape.examine.internal.graphics.draw.Representation;
import org.cytoscape.examine.internal.signal.Subject.SubjectJoin;
import org.cytoscape.examine.internal.signal.Variable;

/**
 * Category of variables that can be manipulated by a user.
 */
public class VariableGroup {
    
    // User friendly name.
    public final String name;
    
    // Join all parameter signals.
    public final SubjectJoin change;
    
    // GUI category.
    SidePane.Category category;
    
    /**
     * Base constructor.
     */
    public VariableGroup(String name) {
        this.name = name;
        this.change = new SubjectJoin();
        this.category = StaticGraphics.sidePane().root().add(name);
    }
    
    private <E> void register(Variable<E> variable,
                              Representation representation) {
        category.representations.add(representation);
        change.add(variable.change);
    }
    
    /**
     * Create a boolean variable with GUI control.
     */
    public Variable<Boolean> createBoolean(String name, boolean initialValue) {
        Variable<Boolean> variable =
                new Variable<Boolean>(initialValue);
        BooleanRepresentation representation =
                new BooleanRepresentation(variable, name);
        
        register(variable, representation);
        
        return variable;
    }
    
    /**
     * Create a generic variable, given a limited set of value choices,
     * with GUI control.
     */
    public <E> Variable<E> create(String name, E initialValue, E... choices) {
        Variable<E> variable =
                new Variable<E>(initialValue);
        ChoiceRepresentation representation =
                new ChoiceRepresentation<E>(variable, name, true, choices);
        
        register(variable, representation);
        
        return variable;
    }
    
    /**
     * Create an enumeration variable with GUI control.
     */
    public <E extends Enum> Variable<E> createEnum(String name, E initialValue) {
        Variable<E> variable =
                new Variable<E>(initialValue);
        ChoiceRepresentation representation =
                new ChoiceRepresentation<E>(variable, name, true,
                        (E[]) variable.get().getClass().getEnumConstants());
        
        register(variable, representation);
        
        return variable;
    }
    
    /**
     * Create an integer variable with GUI control.
     */
    public Variable<Integer> createInteger(String name, int initialValue,
                                         int lowerBound, int upperBound) {
        // Create value range.
        Integer[] choices = new Integer[upperBound - lowerBound + 1];
        for(int i = 0; i < choices.length; i++) {
            choices[i] = lowerBound + i;
        }
        
        Variable<Integer> variable =
                new Variable<Integer>(initialValue);
        ChoiceRepresentation representation =
                new ChoiceRepresentation<Integer>(variable, name, true, choices);
        
        register(variable, representation);
        
        return variable;
    }
    
    /**
     * Create a doubleing point variable with GUI control.
     */
    public Variable<Double> createDouble(String name, double initialValue,
                                       double lowerBound, double upperBound) {
        // Create value range of four.
        Double[] choices = new Double[4];
        double d = upperBound - lowerBound;
        for(int i = 0; i < choices.length; i++) {
            choices[i] = lowerBound + d * (double) i / (double) (choices.length - 1);
        }
        
        Variable<Double> variable =
                new Variable<Double>(initialValue);
        ChoiceRepresentation representation =
                new ChoiceRepresentation<Double>(variable, name, true, choices);
        
        register(variable, representation);
        
        return variable;
    }
    
}
