package com.example.semtechorg.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.semtechorg.backend.*;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
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
    Button showDetail = new Button("Alle Details", this::showDetail);
    Label heading = new Label("Semantic Organization");
    
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
        heading.setStyleName("h1");
        newContact.setStyleName("element");
        showDetail.setStyleName("element");
        filter.setStyleName("element");
        contactList.setStyleName("listMitarbeiter");
        individualForm.setStyleName("listMitarbeiter");
        contactForm.setStyleName("contactForm");
                
        tree.setStyleName("tree");
    	
    	newContact.addClickListener(e -> contactForm.edit(new Mitarbeiter()));

		showDetail.addClickListener(e -> individualForm.show(
				semService.getIndividualByName(((Mitarbeiter) contactList.getSelectedRow()).getName())));
				
        filter.setInputPrompt("Mitarbeiter suchen...");
        
        filter.addTextChangeListener(e -> refreshContacts(e.getText()));

        contactList.setContainerDataSource(new BeanItemContainer<>(Mitarbeiter.class));
        contactList.setColumnOrder("name", "beschreibung", "gehalt", "erfahrungsjahre","email");
        contactList.removeColumn("id");
        contactList.removeColumn("beschreibung");
        contactList.removeColumn("gehalt");
        contactList.removeColumn("erfahrungsjahre");
        
        contactList.setSelectionMode(Grid.SelectionMode.SINGLE);
        contactList.addSelectionListener(e -> contactForm.edit((Mitarbeiter) contactList.getSelectedRow()));
        contactList.addItemClickListener(e -> contactList.setVisible(false));
        
        refreshContacts();
               	
       buildHashMapForTree("<http://www.semanticweb.org/semanticOrg#Sparte>");
       buildHashMapBereichForTree("<http://www.semanticweb.org/semanticOrg#Bereich>");
       
       
       //expand Tree
       for (Object itemId: tree.getItemIds())
           tree.expandItem(itemId);
    }

    private void buildLayout() {
    	      
        HorizontalLayout actions = new HorizontalLayout(filter, newContact,showDetail);
        actions.setWidth("100%");
        filter.setWidth("100%");
        actions.setExpandRatio(filter, 1);
        

        HorizontalLayout secondRow = new HorizontalLayout(tree, contactList, individualForm, contactForm);

        
        VerticalLayout vert = new VerticalLayout(heading, actions, secondRow);
        
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
    
    void fliterContacts(String abteilung) {
    	
    	boolean doFilter = false;
    	for ( Individual i :semService.getIndividualByClass("<http://www.semanticweb.org/semanticOrg#Abteilung>")){
    		doFilter = (doFilter | cutOutName(i.getIndividualName()).equals(abteilung)); 
    	}
    	if (doFilter) 
    		refreshContacts(abteilung);
    	else
    		refreshContacts("");
        
    }

    @WebServlet(urlPatterns = "/*")
    @VaadinServletConfiguration(ui = AddressbookUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
    
    public void buildHashMapForTree(String iri){
    	for (Individual it : semService.getIndividualByClass(iri)){
        	for (OWLConcept concept: it.getObjectProperties()){
        		System.out.println(concept.getName());
				if (concept.getName().equals("<http://www.semanticweb.org/semanticOrg#hatBereich>")){		
					tree.addElements(cutOutName(it.getIndividualName()), cutOutName(concept.getValue()));
				}
			}
		}
    }
    
    public void buildHashMapBereichForTree(String iri){
    	for (Individual it : semService.getIndividualByClass(iri)){
        	for (OWLConcept concept: it.getObjectProperties()){
        		if (concept.getName().equals("<http://www.semanticweb.org/semanticOrg#hatAbteilung>")){
					tree.addElements(cutOutName(it.getIndividualName()), cutOutName(concept.getValue()));			
        		}
			}
		}
    }
    
    public static String cutOutName (String iri){
    	if (iri == null || iri.equals(""))
    		return "";
    	System.out.println(iri);
    	String tmp[] = iri.split("#");
    	return tmp[1].substring(0, tmp[1].length()-1);
    }
    
    public void buildTreeOutOfHashMap(Map<String, String> map){
		
		for (Entry<String, String> it : map.entrySet()){
			String parent = cutOutName(it.getKey().toString());
    		String child = cutOutName(it.getValue().toString());
    		tree.addElements(parent, child);
		}
	}
    
    public void showDetail(Button.ClickEvent event) {
        // Place to call business logic.
    	Notification.show("Alle Details", Type.TRAY_NOTIFICATION);
        contactForm.setVisible(false);
    }    
}
