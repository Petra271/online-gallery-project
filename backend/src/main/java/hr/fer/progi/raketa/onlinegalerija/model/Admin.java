package hr.fer.progi.raketa.onlinegalerija.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name="admin")
public class Admin extends Visitor {

    public Admin(UUID id, String name, String surname, String email,
                 String password, String paypalMail) {
        super(name, surname, email, password, paypalMail);
    }

}