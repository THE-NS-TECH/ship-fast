# -----------------------------------------------------------------------------
# database.tf
# Cria o Azure Database for MySQL Flexible Server.
# -----------------------------------------------------------------------------

resource "azurerm_mysql_flexible_server" "shipfast_mysql" {
  name                   = "shipfast-mysql-server-flexible"
  resource_group_name    = azurerm_resource_group.shipfast_rg.name
  location               = azurerm_resource_group.shipfast_rg.location
  administrator_login    = "shipfast_admin"
  administrator_password = var.db_password
  backup_retention_days  = 7
  delegated_subnet_id    = null
  private_dns_zone_id    = null
  sku_name               = "B_Standard_B1ms"
  version                = "8.0.21"
  zone                   = "1"
}

resource "azurerm_mysql_flexible_database" "shipfast_db" {
  name                = "shipfast"
  resource_group_name = azurerm_resource_group.shipfast_rg.name
  server_name         = azurerm_mysql_flexible_server.shipfast_mysql.name
  charset             = "utf8"
  collation           = "utf8_general_ci"
}

# Regra de Firewall para permitir acesso de qualquer IP (para testes simples/flexibilidade)
resource "azurerm_mysql_flexible_server_firewall_rule" "allow_all" {
  name                = "allow-all-ips"
  resource_group_name = azurerm_resource_group.shipfast_rg.name
  server_name         = azurerm_mysql_flexible_server.shipfast_mysql.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "255.255.255.255"
}
