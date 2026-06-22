# -----------------------------------------------------------------------------
# outputs.tf
# Define os outputs para o GCP.
# -----------------------------------------------------------------------------

output "app_public_ip" {
  description = "IP público da VM"
  value       = google_compute_instance.shipfast_app.network_interface[0].access_config[0].nat_ip
}

output "database_endpoint" {
  description = "IP público da instância do Cloud SQL MySQL"
  value       = google_sql_database_instance.shipfast_mysql.public_ip_address
}
