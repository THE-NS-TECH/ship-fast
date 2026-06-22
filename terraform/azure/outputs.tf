# -----------------------------------------------------------------------------
# outputs.tf
# Define os outputs para a Azure.
# -----------------------------------------------------------------------------

output "app_public_ip" {
  description = "IP público associado à VM"
  value       = azurerm_public_ip.shipfast_pip.ip_address
}

output "database_endpoint" {
  description = "FQDN do servidor MySQL"
  value       = azurerm_mysql_flexible_server.shipfast_mysql.fqdn
}
