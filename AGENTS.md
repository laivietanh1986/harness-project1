# VIETANH.md
## project overview
Spring boot 3.x Rest API Task management , Java 21, Maven , H2 file-based DB
## Quick Start
- Build : './mvnw clean install'
- Run : './mvnw spring-boot:run'
- verify : './init.sh'
## Hard constraints
- data need to be persist after restart (H2 mode  not in-memory mode)
- Health check endpoint need to be return 200 when app ready
- DTO seperate with Entity , do not expose JPA entity over controller
## Read before code
- docs/ARCHITECTURE.md - package structure , layer boundary
- docs/PRODUCT.md - describe 4 feature need to be done and acceptance criterial
- feature_list.json -state of each feature , update after finish
- claude-progress.md - progress history , read first each session  and update after each session .

