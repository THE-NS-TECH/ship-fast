# -----------------------------------------------------------------------------
# database.tf
# Define o banco de dados RDS MySQL na AWS.
# -----------------------------------------------------------------------------

resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "shipfast-db-subnet-group"
  subnet_ids = [aws_subnet.public_1.id, aws_subnet.public_2.id]

  tags = {
    Name = "shipfast-db-subnet-group"
  }
}

resource "aws_db_instance" "shipfast_mysql" {
  identifier             = "shipfast-mysql"
  allocated_storage      = 20
  max_allocated_storage  = 100
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"
  db_name                = var.db_name
  username               = var.db_user
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  skip_final_snapshot    = true

  tags = {
    Name = "shipfast-mysql-rds"
  }
}
