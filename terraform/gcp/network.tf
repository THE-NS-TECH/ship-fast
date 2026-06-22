# -----------------------------------------------------------------------------
# network.tf
# Define a rede (VPC, Subnet e Firewall) no GCP.
# -----------------------------------------------------------------------------

resource "google_compute_network" "shipfast_vpc" {
  name                    = "shipfast-vpc"
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "shipfast_subnet" {
  name          = "shipfast-subnet"
  ip_cidr_range = "10.0.1.0/24"
  region        = var.gcp_region
  network       = google_compute_network.shipfast_vpc.id
}

resource "google_compute_firewall" "allow_ssh_http" {
  name    = "allow-ssh-and-http-8080"
  network = google_compute_network.shipfast_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["22", "8080"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["shipfast-app"]
}
