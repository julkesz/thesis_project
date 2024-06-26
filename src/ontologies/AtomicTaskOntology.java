package ontologies;

import entities.AtomicTask;
import entities.AtomicTaskList;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.*;

public class AtomicTaskOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Atomic-Task-Ontology";

    // AtomicTask schema constants
    public static final String ATOMIC_TASK = "AtomicTask";
    public static final String ATOMIC_TASK_ORDER_NUMBER = "orderNumber";
    public static final String ATOMIC_TASK_DEADLINE = "deadline";
    public static final String ATOMIC_TASK_LENGTH = "length";
    public static final String ATOMIC_TASK_WIDTH = "width";
    public static final String ATOMIC_TASK_EXECUTION_TIME = "executionTime";
    public static final String ATOMIC_TASK_FILAMENT = "filament";

    // TaskList schema constants
    public static final String ATOMIC_TASK_LIST = "atomicTaskList";
    public static final String ATOMIC_TASK_LIST_ATOMIC_TASKS = "atomicTasks";

    private static Ontology instance = new AtomicTaskOntology();

    public static Ontology getInstance() {
        return instance;
    }

    private AtomicTaskOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try {
            // AtomicTask schema
            ConceptSchema atomicTaskSchema = new ConceptSchema(ATOMIC_TASK);
            atomicTaskSchema.add(ATOMIC_TASK_ORDER_NUMBER, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            atomicTaskSchema.add(ATOMIC_TASK_DEADLINE, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            atomicTaskSchema.add(ATOMIC_TASK_LENGTH, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            atomicTaskSchema.add(ATOMIC_TASK_WIDTH, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            atomicTaskSchema.add(ATOMIC_TASK_EXECUTION_TIME, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            atomicTaskSchema.add(ATOMIC_TASK_FILAMENT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            add(atomicTaskSchema, AtomicTask.class);

            // AtomicTaskList schema
            AgentActionSchema atomicTaskListSchema = new AgentActionSchema(ATOMIC_TASK_LIST);
            atomicTaskListSchema.add(ATOMIC_TASK_LIST_ATOMIC_TASKS, (AggregateSchema) getSchema(BasicOntology.SEQUENCE));
            add(atomicTaskListSchema, AtomicTaskList.class);
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }
}

