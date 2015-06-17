package com.example.semtechorg.frontend;

import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.example.semtechorg.backend.*;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.Tree;


/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class ProjektIndividualForm extends FormLayout {

    
    Button cancel = new Button("Cancel", this::cancel);
    TextField name = new TextField();
    List<Individual> in;
    Panel panel;
    OWLComponent olc;
    
    SemanticService semService = SemanticService.createDemoService();
    
    class OWLComponent extends CustomComponent {
    	
    	Individual individual;
    	
        public OWLComponent(Individual ind) {
            // A layout structure used for composition
        	this.individual = ind;
    		Label name = new Label(c(ind.getIndividualName()));
            Panel klassen = new Panel("Klassen");
            VerticalLayout vl = new VerticalLayout();
            klassen.setContent(vl);
            for (String it : ind.getClasses()){
            	Label l = new Label(c(it));
            	l.addStyleName("fancylabel");
            	vl.addComponent(l);
            }
            
            Panel oprop = new Panel("Objekt Properties");
            VerticalLayout vl1 = new VerticalLayout();
            oprop.setContent(vl1);
            for (OWLConcept it : ind.getObjectProperties()){
            	HorizontalLayout hlo = new HorizontalLayout();
            	Label l1 = new Label(c(it.getName()));
            	l1.addStyleName("fancylabel");
            	Label l2 = new Label(c(it.getValue()));
            	l2.addStyleName("fancyvalue");
            	
            	hlo.addComponent(l1);
            	hlo.addComponent(l2);
            	vl1.addComponent(hlo);
            }
            
            Panel dprop = new Panel("Daten Properties");
            VerticalLayout vl2 = new VerticalLayout();
            dprop.setContent(vl2);
            for (OWLConcept it : ind.getDataProperties()){
            	HorizontalLayout hlp = new HorizontalLayout();
            	Label l1 = new Label(c(it.getName()));
            	l1.addStyleName("fancylabel");
            	Label l2 = new Label(c(it.getValue()));
            	l2.addStyleName("fancyvalue");
            	hlp.addComponent(l1);
            	hlp.addComponent(l2);
            	vl2.addComponent(hlp);
            }
            Label l1 = new Label(c(ind.getIndividualName()));
        	l1.addStyleName("fancylabel");
        	vl2.addComponent(l1);
        	
        	
            HorizontalLayout x = new HorizontalLayout();
            x.addComponent(klassen);
            x.addComponent(oprop);
            x.addComponent(dprop);
            
            Panel p = new Panel("Details");
            p.setContent(x);
            p.setSizeFull();
            
            // The composition root MUST be set
            setCompositionRoot(p);
        }
    }

    public ProjektIndividualForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /* Highlight primary actions.
         *
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
        setVisible(false);
    }

    private void buildLayout() {
    	System.out.println("buildlayout");
    	removeAllComponents();
        setSizeUndefined();

        
        HorizontalLayout actions = new HorizontalLayout(cancel);
        actions.setSpacing(true);
        
        panel = new Panel("Details");
        
        addComponents(actions);
        
        VerticalLayout vl = new VerticalLayout();
        
        if (this.in != null){
        	for (Individual i : in){
        		olc = new OWLComponent(i);
            	vl.addComponents(olc);	
        	}        	
        }
        addComponent(vl);
        System.out.println("End build layout");
    }



    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        getUI().contactList.select(null);
        getUI().projektForm.setVisible(false);
        getUI().contactList.setVisible(true);
    }


    @Override
    public AddressbookUI getUI() {
        return (AddressbookUI) super.getUI();
    }
    
    void show(List<Individual> i) {
    	System.out.println("show aufgerufen");

		for (Individual it : i){
    		System.out.println(it.toString());
    	}
    	
        this.in = i;
        buildLayout();
        //olc = new OWLComponent(in);
        //
        setVisible(in != null);
        
        
        getUI().contactList.setVisible(!isVisible());
    }
    
    public static String c(String str){
    	if (str.startsWith("<")) {
    		return AddressbookUI.cutOutName(str);
    	}
    	return str;
    }
}
