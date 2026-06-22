# -----------------------------------------------------------------------------
# database.tf
# Define o cluster de banco de dados gerenciado.
# -----------------------------------------------------------------------------

resource "digitalocean_database_cluster" "shipfast_mysql" {
  name       = "shipfast-mysql-cluster"
  engine     = "mysql"
  version    = "8"
  size       = "db-s-1vcpu-1gb"
  region     = var.region
  node_count = 1
  
  private_network_uuid = digitalocean_vpc.shipfast_vpc.id
}

# Criando o banco de dados 'shipfast' dentro do cluster
resource "digitalocean_database_db" "shipfast_db" {
  cluster_id = digitalocean_database_cluster.shipfast_mysql.id
  name       = "shipfast"
}
