# -----------------------------------------------------------------------------
# network.tf
# Define a rede lógica do ambiente (VPC)
# -----------------------------------------------------------------------------

resource "digitalocean_vpc" "shipfast_vpc" {
  name     = "shipfast-test-vpc"
  region   = var.region
  ip_range = "10.10.10.0/24"
}
