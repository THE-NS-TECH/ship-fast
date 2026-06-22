# -----------------------------------------------------------------------------
# compute.tf
# Define a instância EC2 na AWS.
# -----------------------------------------------------------------------------

resource "aws_key_pair" "shipfast_key" {
  key_name   = "shipfast-key"
  public_key = var.ssh_public_key
}

resource "aws_instance" "shipfast_app" {
  ami                    = "ami-0c7217cdde317cfec" # Ubuntu 22.04 LTS HVM (pode variar por regiao, mas serve de placeholder valido)
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public_1.id
  vpc_security_group_ids = [aws_security_group.app_sg.id]
  key_name               = aws_key_pair.shipfast_key.key_name

  user_data = <<-EOF
              #!/bin/bash
              apt-get update
              apt-get install -y docker.io docker-compose
              systemctl enable docker
              systemctl start docker
              EOF

  tags = {
    Name = "shipfast-app-server"
  }
}
