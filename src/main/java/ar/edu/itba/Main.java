package ar.edu.itba;

import ar.edu.itba.model.PedestrianSimulation;
import ar.edu.itba.model.SFM;
import ar.edu.itba.view.SimulationWriter;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Main {

    private final static String fileDistintion = "5";
    private final static String route = "output/q1/decision_distance/";

    public static void main(String[] args) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream("src/main/resources/config.yml");
        Map<String, Map<String, Object>> conf = yaml.load(inputStream);
        System.out.println("+ Setting simulation...");
        PedestrianSimulation simulation = new PedestrianSimulation(
                                                (int) conf.get("board").get("dimL"),
                                                (int) conf.get("board").get("cellLineCount"),
                                                (double) conf.get("simulation").get("dt"),
                                                (int) conf.get("agents").get("maxCount"),
                                                (double) conf.get("agents").get("mass"),
                                                (double) conf.get("agents").get("minD"),
                                                (double) conf.get("agents").get("maxD"),
                                                (double) conf.get("agents").get("desiredSpeed"),
                                                (int) conf.get("simulation").get("maxIterations"),
                                                (String) conf.get("simulation").get("method"),
                                                new SFM(
                                                        (int) conf.get("sfm").get("kn"),
                                                        (int) conf.get("sfm").get("kt"),
                                                        (double) conf.get("sfm").get("A_a"),
                                                        (double) conf.get("sfm").get("A_w"),
                                                        (double) conf.get("sfm").get("B_a"),
                                                        (double) conf.get("sfm").get("B_w"),
                                                        (double) conf.get("sfm").get("tau")
                                                ),
                                                (int) conf.get("simulation").get("turnstileCount"),
                                                (double) conf.get("simulation").get("turnstileWidth"),
                                                (double) conf.get("simulation").get("corridorLength")
        );
        System.out.println("+ Running simulation...");
        simulation.run();
        try {
            System.out.println("+ Writing simulation output for animation...");
            SimulationWriter.writeSimulationOutput(simulation, route+"particles"+fileDistintion+".xyz");
            System.out.println("+ Writing simulation output for analysis...");
            SimulationWriter.writeEscapeData(simulation, route+"times_sim="+fileDistintion+".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}