package org.cwi.examine.internal.data.domain;

/**
 * Created by kdinkla on 9/28/16.
 */
public class Node extends Element {

    private String module, processes, functions, components, pathways;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getProcesses() {
        return processes;
    }

    public void setProcesses(String processes) {
        this.processes = processes;
    }

    public String getFunctions() {
        return functions;
    }

    public void setFunctions(String functions) {
        this.functions = functions;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getPathways() {
        return pathways;
    }

    public void setPathways(String pathways) {
        this.pathways = pathways;
    }

    @Override
    public String toString() {
        return "Node{" +
                "module='" + module + '\'' +
                ", processes='" + processes + '\'' +
                ", functions='" + functions + '\'' +
                ", components='" + components + '\'' +
                ", pathways='" + pathways + '\'' +
                "} extends " + super.toString();
    }
}
