package com.example;

import com.example.model.Categorie;
import com.example.model.Produit;
import org.h2.tools.Server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        // Lancement du serveur Web intégré H2 pour inspecter la base via le navigateur
        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println(">> Console H2 accessible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("[Erreur] Impossible de lancer le serveur web H2");
            e.printStackTrace();
        }

        // Initialisation de la fabrique JPA liée à l'unité de persistance "hibernate-demo"
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("hibernate-demo");

        // Étape 1 : enregistrement d'un jeu d'essai de catégories et de produits
        insererCategories(emf);
        insererProduits(emf);

        // Étape 2 : interrogation de la base et lecture des entités persistées
        lireCategories(emf);
        lireProduits(emf);

        // Mise en pause du thread principal afin de maintenir la base mémoire accessible
        System.out.println("\n--> Appuyez sur [Entrée] pour quitter l'application...");
        new Scanner(System.in).nextLine();

        // Libération des ressources de persistance
        emf.close();
    }

    private static void insererCategories(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Categorie c1 = new Categorie("Informatique");
            Categorie c2 = new Categorie("Périphériques");
            Categorie c3 = new Categorie("Bureautique");

            em.persist(c1);
            em.persist(c2);
            em.persist(c3);

            em.getTransaction().commit();
            System.out.println(">> Insertion réussie des catégories en base de données !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void lireCategories(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Categorie> categories =
                    em.createQuery("SELECT c FROM Categorie c", Categorie.class)
                            .getResultList();

            System.out.println("\n=== Consultation des catégories enregistrées ===");
            for (Categorie categorie : categories) {
                System.out.println(" - " + categorie);
            }
        } finally {
            em.close();
        }
    }

    private static void insererProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Instanciation de trois exemples d'articles
            Produit p1 = new Produit("Ordinateur Portable", new BigDecimal("849.99"));
            Produit p2 = new Produit("Écran 4K", new BigDecimal("329.50"));
            Produit p3 = new Produit("Clavier Mécanique", new BigDecimal("79.90"));

            // Enregistrement des entités dans le contexte de persistance
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);

            em.getTransaction().commit();
            System.out.println(">> Insertion réussie des articles en base de données !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void lireProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            // Requête orientée objet JPQL pour récupérer l'ensemble du catalogue
            List<Produit> produits =
                    em.createQuery("SELECT p FROM Produit p", Produit.class)
                            .getResultList();

            System.out.println("\n=== Consultation des produits enregistrés ===");
            for (Produit produit : produits) {
                System.out.println(" - " + produit);
            }

            // Recherche directe par clé primaire via EntityManager.find
            System.out.println("\n=== Recherche du produit (identifiant = 2) ===");
            Produit produit = em.find(Produit.class, 2L);
            if (produit != null) {
                System.out.println(" Article trouvé : " + produit);
            } else {
                System.out.println(" Aucun article correspondant à cet identifiant.");
            }
        } finally {
            em.close();
        }
    }

    // =========================================================================
    // Méthodes pour les exercices complémentaires (Page 11 du TP)
    // =========================================================================

    /**
     * Exercice complémentaire 1 : mise à jour du tarif d'un produit
     */
    public static void mettreAJourPrix(EntityManagerFactory emf, Long id, BigDecimal nouveauPrix) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                produit.setPrix(nouveauPrix);
                em.getTransaction().commit();
                System.out.println(">> Prix modifié avec succès pour l'article ID " + id + " : " + nouveauPrix + " €");
            } else {
                System.out.println(">> Impossible de modifier le prix : article ID " + id + " introuvable");
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Exercice complémentaire 2 : suppression physique d'un produit en base
     */
    public static void supprimerProduit(EntityManagerFactory emf, Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                em.remove(produit);
                em.getTransaction().commit();
                System.out.println(">> Article ID " + id + " supprimé de la base.");
            } else {
                System.out.println(">> Échec de la suppression : article ID " + id + " non trouvé");
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Exercice complémentaire 4 : sélection JPQL avec filtre sur intervalle de prix
     */
    public static List<Produit> rechercherParPlageDePrix(EntityManagerFactory emf, BigDecimal min, BigDecimal max) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p WHERE p.prix BETWEEN :min AND :max", Produit.class)
                    .setParameter("min", min)
                    .setParameter("max", max)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
