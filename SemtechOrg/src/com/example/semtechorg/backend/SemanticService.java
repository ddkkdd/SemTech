package com.example.semtechorg.backend;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer; 

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.collections.map.LinkedMap;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.example.semtechorg.frontend.AddressbookUI;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.CustomField;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Separate Java service class. Backend implementation for the address book
 * application, with "detached entities" simulating real world DAO. Typically
 * these something that the Java EE or Spring backend services provide.
 */
// Backend service class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class SemanticService {

	public static String DEFAULT_NAMESPACE = "http://www.semanticweb.org/semanticOrg";
	public static String iri = DEFAULT_NAMESPACE;

	static List<Individual> lnames;

	private static SemanticService instance;
	private static OWLReasoner reasoner;
	private static OWLOntology o;
	private static OWLOntologyManager m;
	private static OWLDataFactory df = OWLManager.getOWLDataFactory();
	private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer(); 

	public static void main(String[] args) throws OWLException, IOException {
		createDemoService();

		// for (Individual it :
		// instance.getIndividualByClass("<http://www.semanticweb.org/semanticOrg#Organisationseinheit>")){
		// System.out.println(it.toString());
		// }

		for (Individual it : instance
				.getIndividualByClass("<http://www.semanticweb.org/semanticOrg#Sparte>")) {
			System.out.println(it.toString());
		}

		for (String s : instance.getIndividualByProperty(
				"<http://www.semanticweb.org/semanticOrg#Southern_Europe>",
				"<http://www.semanticweb.org/semanticOrg#hatBereich>")) {
			System.out.println(s);
		}

		System.out.println("Print object properties...");
		System.out.println(getObjectProperties());


		try {
			// instance.saveDataProperty("Helmberger_Peter", "Erfahrungsjahre",
			// "raben");
			// instance.saveIndividual("Bereich_RD_Bau","Mitarbeiter");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("saved...");
	}

	public static SemanticService createDemoService() {
		if (instance == null) {
			try {
				instance = new SemanticService();
				loadOntology();
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		}

		return instance;
	}

	private HashMap<Long, Individual> individuals = new HashMap<>();
	private HashMap<Long, Mitarbeiter> mitarbeiter = new HashMap<>();
	private long nextId = 0;

	public synchronized List<Individual> findAll(String stringFilter) {
		ArrayList<Individual> arrayList = new ArrayList<Individual>();
		for (Individual i : individuals.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
						|| i.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(i.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(SemanticService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Individual>() {

			@Override
			public int compare(Individual o1, Individual o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		return arrayList;
	}

	public synchronized long count() {
		return individuals.size();
	}

	public synchronized void delete(Individual value) {
		individuals.remove(value.getId());
	}

	public synchronized void save(Individual entry) {
		if (entry.getId() == null) {
			entry.setId(nextId++);
		}
		try {
			// entry = (Individual) BeanUtils.cloneBean(entry);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		individuals.put(entry.getId(), entry);
	}

	public synchronized void clear() {
		individuals = new HashMap<>();
		mitarbeiter = new HashMap<>();
	}

	public static List<String> getObjectProperties() throws OWLOntologyCreationException {

		List<String> list = new LinkedList<String>();

		for (OWLObjectProperty it : o.getObjectPropertiesInSignature(true)) {
			
			list.add(it.getIRI().toString().substring(it.getIRI().toString().indexOf("#") + 1));
		}
		return list;
	}

	public static void loadOntology() throws OWLOntologyCreationException {
		m = OWLManager.createOWLOntologyManager();

		String file = "C:\\Users\\Peter\\Dropbox\\SemTech SS15\\Miniprojekt 2\\Mini2_OWL.owl";
		//String file = "/Users/Daniel/Dropbox/SemTech SS15/Miniprojekt 2/Mini2_OWL.owl";

		instance.clear();
		o = m.loadOntologyFromOntologyDocument(new File(file));
		reasoner = new Reasoner(o);
		System.out.println("Reasoner-Name: " + reasoner.getReasonerName());
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
				InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS,
				InferenceType.OBJECT_PROPERTY_HIERARCHY);

		
		OWLDataFactory factory = m.getOWLDataFactory();
		OWLClass Thing = factory.getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing"));
		OWLObjectProperty skip = factory.getOWLTopObjectProperty();
		Set<OWLNamedIndividual> individuals = reasoner.getInstances(Thing, false).getFlattened();

		long j = 0;
		boolean isMA = false;

		for (OWLNamedIndividual ind : individuals) {

			List<OWLConcept> dpm = new LinkedList<OWLConcept>();
			List<OWLConcept> opm = new LinkedList<OWLConcept>();

			j++;
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataProperties = ind
					.getDataPropertyValues(o);
			for (Entry<OWLDataPropertyExpression, Set<OWLLiteral>> d : dataProperties.entrySet()) {
				if (d.getKey() != null) {
					for (OWLLiteral s : d.getValue()) {
						dpm.add(new OWLConcept(d.getKey().toString(), s.getLiteral()));
					}
				}
			}

			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> assertedValues = ind
					.getObjectPropertyValues(o);
			
			for (OWLObjectProperty objProp : o.getObjectPropertiesInSignature(true)) {
				if (!objProp.equals(skip)){
					for (OWLNamedIndividual i : reasoner.getObjectPropertyValues(ind, objProp)
							.getFlattened()) {
							boolean asserted = false;
							if (assertedValues.get(objProp) != null) {
								asserted = assertedValues.get(objProp).contains(ind);
							}
							opm.add(new OWLConcept(objProp.toString(), i.toString()));
					}
				}
			}					
			
			isMA = false;
			List<String> classes = new LinkedList<String>();
			NodeSet<OWLClass> owlclasses = reasoner.getTypes(ind, false);
			for (Node<OWLClass> s : owlclasses) {
				for (OWLClass c : s) {
					classes.add(c.toString());
					isMA = isMA | c.toString().equals("<http://www.semanticweb.org/semanticOrg#Mitarbeiter>");
				}
			}

			Individual i = new Individual(j, ind.toString(), dpm, opm, classes);

			if (isMA) {
				instance.saveMA(i.createMitarbeiter());
			}

			instance.save(i);

		}

	}

	public void getIndividual(String iname) {

		OWLNamedIndividual subject = df.getOWLNamedIndividual(IRI.create(iri + "#" + iname));
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> assertedValues = subject
				.getObjectPropertyValues(o);
		
		for (OWLObjectProperty objProp : o.getObjectPropertiesInSignature(true)) {
			for (OWLNamedIndividual ind : reasoner.getObjectPropertyValues(subject, objProp).getFlattened()) {
				boolean asserted = false;
				if (assertedValues.get(objProp) != null) {
					asserted = assertedValues.get(objProp).contains(ind);
				}
			}
		}
	}

	public synchronized List<Mitarbeiter> findAllMA(String stringFilter) {
		
		try {
			loadOntology();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Mitarbeiter> arrayList = new ArrayList<Mitarbeiter>();
		for (Mitarbeiter i : mitarbeiter.values()) {
			
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
						|| i.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(i.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(SemanticService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Mitarbeiter>() {

			@Override
			public int compare(Mitarbeiter o1, Mitarbeiter o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		return arrayList;
	}

	public synchronized long countMA() {
		return individuals.size();
	}

	public synchronized void deleteMA(Mitarbeiter value) {
		individuals.remove(value.getId());
	}

	public synchronized void saveMA(Mitarbeiter entry) {
		if (entry.getId() == null) {
			entry.setId(nextId++);
		}
		try {
			entry = (Mitarbeiter) BeanUtils.cloneBean(entry);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		mitarbeiter.put(entry.getId(), entry);
		
	}

	public List<Individual> getIndividualByClass(String classname) {
		List<Individual> classIndividuals = new ArrayList<>();
		for (Entry<Long, Individual> s : individuals.entrySet()) {
			if (s.getValue().isClassMember(classname)) {
				classIndividuals.add(s.getValue());
			}
		}
		return classIndividuals;
	}

	public List<String> getIndividualByProperty(String iri, String pname) {
		for (Individual i : individuals.values()) {
			if (i.getIndividualName().equals(iri)) {
				return i.getIndividualByProperty(pname);
			}
		}
		return null;
	}
	
	public List<String> getIndividualByProperty2(String pname) {
		List<String> list = new LinkedList<String>();
		if ((pname == null) || (pname.equals("")))
			return list;
		OWLObjectProperty objectProperty = df
				.getOWLObjectProperty(IRI.create(iri + "#" + pname));

		for (Node<OWLClass> range : reasoner.getObjectPropertyRanges(objectProperty, true)){
			for (OWLClass n : range){
				for (Individual i : getIndividualByClass(n.toString())){
					
					list.add(AddressbookUI.cutOutName(i.getIndividualName()));					
				}
			}
		}
		return list;
	}

	public Individual getIndividualByName(String iname) {
		getIndividual(iname);
		String name = "<" + iri + "#" + iname + ">";

		for (Individual i : individuals.values()) {
			if (i.getIndividualName().equals(name)) {
				return i;
			}
		}
		return null;
	}

	public void saveObjectProperty(String sString, String opString, String oString)
			throws Exception {
		OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(iri + "#" + sString));
		OWLIndividual object = df.getOWLNamedIndividual(IRI.create(iri + "#" + oString));

		OWLObjectProperty objectProperty = df
				.getOWLObjectProperty(IRI.create(iri + "#" + opString));

		OWLAxiom assertion = df.getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object);

		AddAxiom addAxiomChange = new AddAxiom(o, assertion);
		m.applyChange(addAxiomChange);

		reasoner.flush();

		System.out.println("Reasoner consistent? " + reasoner.isConsistent());
		if (!reasoner.isConsistent()) {
			RemoveAxiom removeAxiom = new RemoveAxiom(o, assertion);
			m.applyChange(removeAxiom);
			throw new Exception("Axiom (" + sString + " " + opString + " " + oString
					+ ") würde zu einer Inkonsitenz in der Ontologie führen");
		} else {
			m.saveOntology(o);
		}

	}

	public void saveDataProperty(String sString, String opString, String value) throws Exception {
		OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(iri + "#" + sString));
		OWLLiteral literal = df.getOWLLiteral(value);

		try {
			int foo = Integer.parseInt(value);
			literal = df.getOWLLiteral(foo);
		} catch (NumberFormatException e) {
			//nothing
		}

		OWLDataProperty dataProperty = df.getOWLDataProperty(IRI.create(iri + "#" + opString));
		OWLAxiom assertion = df.getOWLDataPropertyAssertionAxiom(dataProperty, subject, literal);

		AddAxiom addAxiomChange = new AddAxiom(o, assertion);
		System.out.println("Reasoner consistent? " + reasoner.isConsistent());

		m.applyChange(addAxiomChange);
		reasoner.flush();

		System.out.println("Reasoner consistent? " + reasoner.isConsistent());
		if (!reasoner.isConsistent()) {
			RemoveAxiom removeAxiom = new RemoveAxiom(o, assertion);
			m.applyChange(removeAxiom);
			System.out.println(addAxiomChange);
			throw new Exception("Axiom (" + sString + " " + opString + " " + value
					+ ") würde zu einer Inkonsitenz in der Ontologie führen");
		} else {
			m.saveOntology(o);
		}
	}

	public void saveIndividual(String sString, String cString) throws Exception {

		OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(iri + "#" + sString));

		OWLClass owlclass = df.getOWLClass(IRI.create(iri + "#" + cString));
		OWLAxiom assertion = df.getOWLClassAssertionAxiom(owlclass, subject);

		AddAxiom addAxiomChange = new AddAxiom(o, assertion);
		System.out.println("Reasoner consistent? " + reasoner.isConsistent());

		m.applyChange(addAxiomChange);
		reasoner.flush();

		System.out.println("Reasoner consistent? " + reasoner.isConsistent());
		if (!reasoner.isConsistent()) {
			RemoveAxiom removeAxiom = new RemoveAxiom(o, assertion);
			m.applyChange(removeAxiom);
			System.out.println(addAxiomChange);
			throw new Exception("Axiom (" + sString + " " + cString
					+ ") würde zu einer Inkonsitenz in der Ontologie führen");
		} else {
			m.saveOntology(o);
		}
	}
}
