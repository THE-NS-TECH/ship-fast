# -----------------------------------------------------------------------------
# outputs.tf
# Define as saídas (outputs) para o Terraform AWS.
# -----------------------------------------------------------------------------

output "app_public_ip" {
  description = "IP público da instância EC2"
  value       = aws_instance.shipfast_app.public_ip
}

output "database_endpoint" {
  description = "Endpoint de conexao do banco RDS"
  value       = aws_db_instance.shipfast_mysql.endpoint
}
