package com.example.semtechorg.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.example.semtechorg.backend.*;
import com.example.semtechorg.backend.Mitarbeiter;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.ExpandEvent;

import javax.servlet.annotation.WebServlet;

import org.semanticweb.owlapi.model.*;

/* User Interface written in Java.
 *
 * Define the user interface shown on the Vaadin generated web page by extending the UI class.
 * By default, a new UI instance is automatically created when the page is loaded. To reuse
 * the same instance, add @PreserveOnRefresh.
 */
@Title("Semantic Organization")
@Theme("semtechorgtheme")
public class AddressbookUI extends UI {

    TextField filter = new TextField();
    
    Grid contactList = new Grid();
    Button newContact = new Button("Neuer Mitarbeiter");
    Button showDetail = new Button("Alle Details");
    
    ContactForm contactForm = new ContactForm();
    IndividualForm individualForm = new IndividualForm();
    
    MyTree tree = new MyTree();
    Map<String, String> sparteMap = new HashMap<String, String>();
    Map<String, String> bereichMap = new HashMap<String, String>();
    
    
    SemanticService semService = SemanticService.createDemoService();

    @Override
    protected void init(VaadinRequest request) {
        configureComponents();
        buildLayout();
        
       
    }


    private void configureComponents() {
        newContact.addClickListener(e -> contactForm.edit(new Mitarbeiter()));
        showDetail.addClickListener(e -> individualForm.show(semService.getIndividualByName(
        		((Mitarbeiter) contactList.getSelectedRow()).getName())));

        filter.setInputPrompt("Filter contacts...");
        filter.addTextChangeListener(e -> refreshContacts(e.getText()));

        contactList.setContainerDataSource(new BeanItemContainer<>(Mitarbeiter.class));
        contactList.setColumnOrder("name", "beschreibung", "gehalt", "erfahrungsjahre","email");
        contactList.removeColumn("id");
        contactList.removeColumn("beschreibung");
        contactList.removeColumn("gehalt");
        contactList.removeColumn("erfahrungsjahre");
        
        contactList.setSelectionMode(Grid.SelectionMode.SINGLE);
        //contactList.addSelectionListener(e -> contactForm.edit((Mitarbeiter) contactList.getSelectedRow()));
        refreshContacts();
               	
       sparteMap = buildHashMapForTree("<http://www.semanticweb.org/semanticOrg#Sparte>");
       bereichMap = buildHashMapForTree("<http://www.semanticweb.org/semanticOrg#Bereich>");
       
       buildTreeOutOfHashMap(sparteMap);
   	   buildTreeOutOfHashMap(bereichMap);
   	    	
       //expand Tree
       for (Object itemId: tree.getItemIds())
           tree.expandItem(itemId);
    }

    private void buildLayout() {
    	      
        HorizontalLayout actions = new HorizontalLayout(filter, newContact,showDetail);
        actions.setWidth("100%");
        filter.setWidth("100%");
        actions.setExpandRatio(filter, 1);
        
        contactList.setWidth("50%");
        contactList.setHeight("50%");
        
        HorizontalLayout thirdRow = new HorizontalLayout(tree, contactList, individualForm);
        
        HorizontalLayout secondRow = new HorizontalLayout(tree, contactList,individualForm, contactForm);
        
        VerticalLayout vert = new VerticalLayout(actions, secondRow,thirdRow);
        
        HorizontalLayout hor = new HorizontalLayout(vert);
       
        setContent(hor);
    }

    void refreshContacts() {
        refreshContacts(filter.getValue());
    }

    private void refreshContacts(String stringFilter) {
        contactList.setContainerDataSource(new BeanItemContainer<>(
                Mitarbeiter.class, semService.findAllMA(stringFilter)));
        contactForm.setVisible(false);
    }

    @WebServlet(urlPatterns = "/*")
    @VaadinServletConfiguration(ui = AddressbookUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
    
    public HashMap<String, String> buildHashMapForTree(String iri){
    	Map <String, String> map = new HashMap<String, String>();
    	
    	for (Individual it : semService.getIndividualByClass(iri)){
        	for (OWLConcept concept: it.getObjectProperties()){
				
				map.put(it.getIndividualName(), concept.getValue());
				
				System.out.println("Sparte: "+it.getIndividualName());
				System.out.println("Bereich: "+concept.getValue()+"\n");
			}
		}
    	return (HashMap<String, String>) map;
    }
    
    public static String cutOutName (String iri){
    	System.out.println(iri);
    	String tmp[] = iri.split("#");
    	return tmp[1].substring(0, tmp[1].length()-1);
    }
    
    public void buildTreeOutOfHashMap(Map<String, String> map){
		Iterator it = map.entrySet().iterator();
    	while (it.hasNext()){
    		Map.Entry entry = (Map.Entry)it.next();
    		String parent = cutOutName(entry.getKey().toString());
    		String child = cutOutName(entry.getValue().toString());
    		tree.addElements(parent, child);
    	}
	}
}
