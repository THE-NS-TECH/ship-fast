# -----------------------------------------------------------------------------
# database.tf
# Cria a instancia de Cloud SQL MySQL no GCP.
# -----------------------------------------------------------------------------

resource "google_sql_database_instance" "shipfast_mysql" {
  name             = "shipfast-mysql-instance"
  database_version = "MYSQL_8_0"
  region           = var.gcp_region

  settings {
    tier = "db-f1-micro"

    ip_configuration {
      ipv4_enabled = true
      
      # Opcionalmente adiciona regras de acesso
      authorized_networks {
        name  = "all"
        value = "0.0.0.0/0"
      }
    }
  }

  deletion_protection = false
}

resource "google_sql_database" "shipfast_db" {
  name     = "shipfast"
  instance = google_sql_database_instance.shipfast_mysql.name
}

resource "google_sql_user" "shipfast_user" {
  name     = "shipfast_user"
  instance = google_sql_database_instance.shipfast_mysql.name
  host     = "%"
  password = var.db_password
}
