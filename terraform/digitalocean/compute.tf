# -----------------------------------------------------------------------------
# compute.tf
# Define a infraestrutura de computação (Droplets)
# Onde a aplicação e os containers docker irão rodar.
# -----------------------------------------------------------------------------

resource "digitalocean_droplet" "shipfast_app" {
  name     = "shipfast-app-server"
  image    = "ubuntu-22-04-x64"
  region   = var.region
  size     = var.droplet_size
  vpc_uuid = digitalocean_vpc.shipfast_vpc.id
  ssh_keys = [digitalocean_ssh_key.shipfast_key.id]
  
  tags = ["shipfast", "app"]

  # Script executado no boot para instalar docker e rodar a app
  user_data = <<-EOF
              #!/bin/bash
              apt-get update
              apt-get install -y docker.io docker-compose
              systemctl enable docker
              # Aqui os sources seriam baixados (ex: git clone)
              # e o comando `docker-compose up -d --build` seria rodado.
              EOF
}
