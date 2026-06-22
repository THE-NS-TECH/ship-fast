# -----------------------------------------------------------------------------
# outputs.tf
# Define os valores que o Terraform vai expor ao terminar a execução
# -----------------------------------------------------------------------------

output "app_public_ip" {
  description = "O IP público do App Server onde a API estará rodando"
  value       = digitalocean_droplet.shipfast_app.ipv4_address
}

output "database_host" {
  description = "O host do banco de dados gerenciado"
  value       = digitalocean_database_cluster.shipfast_mysql.host
  sensitive   = true
}

output "database_password" {
  description = "Senha default do BD (gerada pelo provedor se não fixada)"
  value       = digitalocean_database_cluster.shipfast_mysql.password
  sensitive   = true
}
