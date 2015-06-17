package com.example.semtechorg.frontend;

import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.vaadin.client.ui.richtextarea.VRichTextToolbar.Strings;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.example.semtechorg.backend.*;
import com.google.common.escape.Escapers;
import com.google.common.html.HtmlEscapers;
import com.google.gwt.thirdparty.guava.common.escape.Escaper;
import com.google.gwt.thirdparty.streamhtmlparser.util.HtmlUtils;
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
public class ContactForm extends FormLayout {

    Button save = new Button("Speichern", this::save);
    Button cancel = new Button("Abbrechen", this::cancel);
    
    Label heading = new Label("Bearbeiten");
    
    Button addRow = new Button("Property hinzufuegen",this::addRow);
    TextField name = new TextField("Name");
    TextField beschreibung = new TextField("Beschreibung");
    TextField email = new TextField("Email");
    TextField gehalt = new TextField("Gehalt");
    TextField erfahrungsjahre = new TextField("Erfahrungsjahre");
    ComboBox abteilung = new ComboBox("Abteilung");
    
    
    EmployeeRow[] emps = new EmployeeRow[10];
    int rows = 0;
    
    

    SemanticService semService = SemanticService.createDemoService();
    
    Mitarbeiter mitarbeiter;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Mitarbeiter> formFieldBindings;

    
    class EmployeeRow extends CustomComponent {
    	private TextField objekt;
    	private ComboBox select;
    	
        public EmployeeRow(String property) {
            // A layout structure used for composition
            Panel panel = new Panel("neues Property");
            HorizontalLayout hl = new HorizontalLayout();
            HorizontalLayout vl = new HorizontalLayout();
            panel.setContent(vl);

            // Compose from multiple components
            select = new ComboBox("Beziehung");
            try {
				select.addItems(SemanticService.getObjectProperties());
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
            
            
            hl.addComponent(select);
            objekt = new TextField("Objekt");
            hl.addComponent(objekt);
            
            vl.addComponent(hl);
            // Set the size as undefined at all levels
            panel.getContent().setSizeUndefined();
            panel.setSizeUndefined();
            setSizeUndefined();
            

            // The composition root MUST be set
            setCompositionRoot(panel);
        }
    }

    public ContactForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /* Highlight primary actions.
         *
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
    	heading.setStyleName("h3");
    	
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        setVisible(false);       
    }

    private void buildLayout() {
        setSizeUndefined();
        
        
        
        //setMargin(new MarginInfo(true, true, true, true));

        HorizontalLayout actions = new HorizontalLayout(save, cancel, addRow);
        actions.setSpacing(true);
        
        name.setRequired(true);
        beschreibung.setRequired(true);
        email.setRequired(true);
        gehalt.setRequired(true);
        erfahrungsjahre.setRequired(true);
        abteilung.setRequired(true);
        
        GridLayout grid = new GridLayout(6,2);
        
		addComponents(heading, actions, name, beschreibung, email, gehalt, erfahrungsjahre,abteilung);
    }


    public void save(Button.ClickEvent event) {
        try {
        	name.commit();
        	beschreibung.commit();
        	email.commit();
        	gehalt.commit();
        	erfahrungsjahre.commit();
        	abteilung.commit();
        	
            formFieldBindings.commit();
            
            
            getUI().semService.saveIndividual(this.name.getValue(), "Mitarbeiter");
            getUI().semService.saveDataProperty(this.name.getValue(), "Beschreibung", this.beschreibung.getValue());
            getUI().semService.saveDataProperty(this.name.getValue(), "hatEmailAdresse", this.email.getValue());
            getUI().semService.saveDataProperty(this.name.getValue(), "Gehalt", this.gehalt.getValue());
            getUI().semService.saveDataProperty(this.name.getValue(), "Erfahrungsjahre", this.erfahrungsjahre.getValue());
            getUI().semService.saveObjectProperty(this.name.getValue(), "arbeitetInAbteilung",(String) this.abteilung.getValue());
            
            for (int i = 0; i < rows && i < 10; i++) {
            	getUI().semService.saveObjectProperty(this.name.getValue(),(String)emps[i].select.getValue(),emps[i].objekt.getValue());
            }

            String msg = String.format("'%s %s' gespeichert.",
            		this.name.getValue(),
            		this.beschreibung.getValue());
            Notification.show(msg,Type.TRAY_NOTIFICATION);
            getUI().refreshContacts();
            getUI().contactList.setVisible(true);
        } catch (FieldGroup.CommitException e) {
        
        } catch (InvalidValueException  e) {
        	new Notification("Alle Felder müssen befüllt sein",
        			
        			"",
        		    Notification.TYPE_ERROR_MESSAGE, true)
        		    .show(Page.getCurrent());
            
        } catch (Exception e){
        	new Notification("Fehler: ",
        		    e.getMessage(),
        		    Notification.TYPE_ERROR_MESSAGE, true)
        		    .show(Page.getCurrent());
        }   
    }

    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        getUI().contactList.select(null);
        getUI().contactForm.setVisible(false);
        getUI().contactList.setVisible(true);
    }

    void edit(Mitarbeiter mitarbeiter) {
        this.mitarbeiter = mitarbeiter;
        for (Individual i :  getUI().semService.getIndividualByClass("<http://www.semanticweb.org/semanticOrg#Abteilung>")){
        	abteilung.addItems(AddressbookUI.cutOutName(i.getIndividualName()));
        }
        if(mitarbeiter != null) {
            // Bind the properties of the contact POJO to fiels in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(mitarbeiter, this);
            name.focus();
        }
        setVisible(mitarbeiter != null);
    }
    
    public void addRow(Button.ClickEvent event) {
    	rows++;
    	for (int i = 0; i < rows && i < 10; i++) {
			if (emps[i] == null) 
				emps[i] = new EmployeeRow("new");
			addComponent(emps[i]);
		}
    }
    

    @Override
    public AddressbookUI getUI() {
        return (AddressbookUI) super.getUI();
    }

    
    

}
