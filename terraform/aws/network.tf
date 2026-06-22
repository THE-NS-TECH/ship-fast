# -----------------------------------------------------------------------------
# network.tf
# Configura a rede (VPC, Subnets, SG) para a AWS.
# -----------------------------------------------------------------------------

resource "aws_vpc" "shipfast_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "shipfast-vpc"
  }
}

resource "aws_internet_gateway" "shipfast_igw" {
  vpc_id = aws_vpc.shipfast_vpc.id

  tags = {
    Name = "shipfast-igw"
  }
}

resource "aws_subnet" "public_1" {
  vpc_id                  = aws_vpc.shipfast_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "shipfast-public-1"
  }
}

resource "aws_subnet" "public_2" {
  vpc_id                  = aws_vpc.shipfast_vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "${var.aws_region}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "shipfast-public-2"
  }
}

resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.shipfast_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.shipfast_igw.id
  }

  tags = {
    Name = "shipfast-public-rt"
  }
}

resource "aws_route_table_association" "public_1_assoc" {
  subnet_id      = aws_subnet.public_1.id
  route_table_id = aws_route_table.public_rt.id
}

resource "aws_route_table_association" "public_2_assoc" {
  subnet_id      = aws_subnet.public_2.id
  route_table_id = aws_route_table.public_rt.id
}

# Security Group para a Instância EC2
resource "aws_security_group" "app_sg" {
  name        = "shipfast-app-sg"
  description = "Allow HTTP and SSH access"
  vpc_id      = aws_vpc.shipfast_vpc.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Spring Boot App"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "shipfast-app-sg"
  }
}

# Security Group para o RDS (MySQL)
resource "aws_security_group" "db_sg" {
  name        = "shipfast-db-sg"
  description = "Allow MySQL access from EC2"
  vpc_id      = aws_vpc.shipfast_vpc.id

  ingress {
    description     = "MySQL from EC2"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "shipfast-db-sg"
  }
}
