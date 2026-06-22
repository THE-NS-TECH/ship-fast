# -----------------------------------------------------------------------------
# compute.tf
# Cria a VM (Compute Engine) no GCP.
# -----------------------------------------------------------------------------

resource "google_compute_instance" "shipfast_app" {
  name         = "shipfast-app-server"
  machine_type = var.machine_type
  zone         = var.gcp_zone

  tags = ["shipfast-app"]

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2204-lts"
    }
  }

  network_interface {
    subnetwork = google_compute_subnetwork.shipfast_subnet.id

    access_config {
      // Ephemeral public IP
    }
  }

  metadata = {
    ssh-keys = "${var.ssh_user}:${var.ssh_public_key}"
  }

  metadata_startup_script = <<-EOF
                            #!/bin/bash
                            apt-get update
                            apt-get install -y docker.io docker-compose
                            systemctl enable docker
                            systemctl start docker
                            EOF
}
