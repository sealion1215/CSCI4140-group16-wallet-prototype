#!/bin/bash
#test1123
function download_electrum_server(){
	echo "Downloading electrum server..."
	git clone https://github.com/reddcoin-project/reddcoin-electrum-server.git
	cd "$server_dir/reddcoin-electrum-server"
	#sudo "./electrum-configure"
	sudo apt-get install python-setuptools python-openssl python-leveldb libleveldb-dev
	sudo easy_install jsonrpclib irc plyvel
	sudo python "./setup.py" install
	echo "electrum download finished."
}
function create_DB_dir(){
	echo "Creating database directory..."
	sudo mkdir -p $electrum_DB_dir
	sudo chmod 777 -R $electrum_DB_dir
	cd $electrum_DB_dir
	subdir=( "./addr" "./hist" "./undo" "./utxo" )
	for name in "${subdir[@]}"; do
		mkdir $name
		sudo chmod 777 "$name"
		cd $name
		touch "./LOCK"
		sudo chmod 666 "./LOCK"
		cd "../"
	done
	echo "db root created."
	echo ""
}
function make_reddconf(){
	echo "Writing reddcoin.conf file..."
	mkdir -p $reddcoin_conf_dir
	sudo chmod 777 -R $reddcoin_conf_dir
	cd $reddcoin_conf_dir
	if [[ ! -e "./reddcoin.conf" ]]; then
		sudo touch "./reddcoin.conf"
	else
		echo "conf file exists."
	fi
	sudo chmod 666 "./reddcoin.conf"
	read -p "rpcuser(default: rpcuser): " rpcuser
	if [[ -z "$rpcuser" ]]; then
		rpcuser="rpcuser"
	fi
	sudo echo "rpcuser=$rpcuser" > "./reddcoin.conf"
	read -p "rpcpassword(default: 123456): " rpcpassword
	if [[ -z "$rpcpassword" ]]; then
		rpcpassword="123456"
	fi
	sudo echo "rpcpassword=$rpcpassword" >> "./reddcoin.conf"
	#test directory
	sudo echo "datadir=/media/sealion1215/OS/reddcoin/.reddcoin" >> "./reddcoin.conf"
	sudo echo "rpcallowip=$rpcallowip" >> "./reddcoin.conf"
	sudo echo "rpcport=$rpcport" >> "./reddcoin.conf" 
	sudo echo "daemon=1" >> "./reddcoin.conf"
	sudo echo "txindex=1" >> "./reddcoin.conf"
	sudo echo "staking=0" >> "./reddcoin.conf"
	read -p "Number of node IP(default: 1): " node_number
	if [[ -z "$node_number" ]]; then
		node_number="1"
	elif [[ ! "$node_number" =~ ^([1-9]{1})([0-9]*)$ ]]; then
		node_number="1"
	fi
	for (( counter=0; counter < $node_number; counter++ ))
	do
		input_IP_address
	done
	echo "Finish making reddcoin.conf."
	echo ""
}
function make_electrumconf(){
	echo "Creating electrum.conf file..."
	mkdir -p $electrum_conf_dir
	cd $electrum_conf_dir
	if [[ ! -e "./electrum.conf" ]]; then
		sudo touch "./electrum.conf"
	else
		echo "conf file exists."
	fi
	sudo chmod 666 "./electrum.conf"
	sudo echo "[server]" > "./electrum.conf"
	while true; do
		read -p "Please enter your system user name: " sys_username
		if [[ ! -z "$sys_username" ]]; then 
			break
		fi
	done
	sudo echo "username = $sys_username" >> "./electrum.conf"
	read -p "Host IP Address to be reached from outside(default: 127.0.0.1): " fqdn_IP
	if [[ -z "$fqdn_IP" ]]; then
		fqdn_IP="127.0.0.1"
	else 
		check_IP $fqdn_IP
		valid_IP=$?
		if [[ $valid_IP == 0 ]]; then
			fqdn_IP="127.0.0.1"
		fi
	fi
	sudo echo "host = $fqdn_IP" >> "./electrum.conf"
	read -p "RPC port(default: 8000): " elect_rpc_port
	if [[ -z "$elect_rpc_port" ]]; then
		elect_rpc_port="8000"
	else 
		check_port $elect_rpc_port
		valid_port=$?
		if [[ $valid_port == 0 ]]; then
			elect_rpc_port="8000"
		fi
	fi
	sudo echo "electrum_rpc_port = $elect_rpc_port" >> "./electrum.conf"
	sudo echo "password = $rpcpassword" >> "./electrum.conf" 
	read -p "Stratum TCP port(default: 50001): " tcp_port
	if [[ -z "$tcp_port" ]]; then
		tcp_port="50001"
	else 
		check_port $tcp_port
		valid_port=$?
		if [[ $valid_port == 0 ]]; then
			tcp_port="50001"
		fi
	fi
	sudo echo "stratum_tcp_port = $tcp_port" >> "./electrum.conf"
	read -p "Stratum TCP SSL port(default: 50002): " tcp_ssl_port
	if [[ -z "$tcp_ssl_port" ]]; then
		tcp_ssl_port="50002"
	else 
		check_port $tcp_ssl_port
		valid_port=$?
		if [[ $valid_port == 0 ]]; then
			tcp_ssl_port="50002"
		fi
	fi
	sudo echo "stratum_tcp_ssl_port = $tcp_ssl_port" >> "./electrum.conf"
	read -p "Stratum HTTP port(default: 8081): " http_port
	if [[ -z "$http_port" ]]; then
		http_port="8081"
	else 
		check_port $http_port
		valid_port=$?
		if [[ $valid_port == 0 ]]; then
			http_port="8081"
		fi
	fi
	sudo echo "stratum_http_port = $http_port" >> "./electrum.conf"
	read -p "Stratum HTTP SSL port(default: 8082): " http_ssl_port
	if [[ -z "$http_ssl_port" ]]; then
		http_ssl_port="8082"
	else 
		check_port $http_ssl_port
		valid_port=$?
		if [[ $valid_port == 0 ]]; then
			http_ssl_port="8082"
		fi
	fi
	sudo echo "stratum_http_ssl_port = $http_ssl_port" >> "./electrum.conf"
	sudo echo "#report_host = $fqdn_IP" >> "./electrum.conf"
	sudo echo "#report_stratum_tcp_port = $tcp_port" >> "./electrum.conf" 
	sudo echo "#report_stratum_tcp_ssl_port = $tcp_ssl_port" >> "./electrum.conf"
	sudo echo "#report_stratum_http_port = $http_port" >> "./electrum.conf"
	sudo echo "#report_stratum_http_ssl_port = $http_ssl_port" >> "./electrum.conf"
	sudo echo "banner = Welcome to Electrum!" >> "./electrum.conf"
	sudo echo "banner_file = /etc/electrum.banner" >> "./electrum.conf"
	IFS='.' read -ra substr_array <<< "$fqdn_IP"
	irc_nick=""
	for i in "${substr_array[@]}"; do
		irc_nick+=$i
		irc_nick+="_"
	done
	irc_nick=${irc_nick:0:$((${#irc_nick} - 1))}
	sudo echo "irc_nick = $irc_nick" >> "./electrum.conf"
	sudo echo "irc = yes" >> "./electrum.conf"
	while true; do
		read -p "Do you have any certificate file and its signing key file?(Y/N) " crt_prepared
		if [[ ! -z "$crt_prepared" ]]; then 
			if [ $crt_prepared == "Y" -o $crt_prepared == "N" ]; then	
				break
			else
				echo "invalid input"
			fi
		fi
	done
	if [[ $crt_prepared == "Y" ]]; then
		while true; do
			read -p "Location of the certificate file: " crt_location
			if [[ ! -z "$crt_location" ]]; then
				break
			fi
		done
		while true; do
			read -p "Location of the key file: " key_location
			if [[ ! -z "$key_location" ]]; then
				break
			fi
		done
		crt_location=${crt_location//"~"/$HOME}
		key_location=${key_location//"~"/$HOME}
	else
		make_crt_key
		crt_location="$server_dir/reddcoin-electrum-server/electrum-server.crt"
		key_location="$server_dir/reddcoin-electrum-server/electrum-server.key"
	fi
	sudo echo "ssl_certfile = $crt_location" >> "./electrum.conf"
	sudo echo "ssl_keyfile = $key_location" >> "./electrum.conf"
	sudo echo "logfile = $electrum_logfile_dir" >> "./electrum.conf"
	read -p "Donation address(default: ): " donation_address
	sudo echo "donation_address = $donation_address" >> "./electrum.conf"
	sudo echo "[leveldb]" >> "./electrum.conf"
	sudo echo "path = $electrum_DB_dir" >> "./electrum.conf"
	read -p "Pruning limit(default: 100): " pruning_limit
	if [[ -z "pruning_limit" ]]; then
		pruning_limit="100"
	elif [[ ! "$pruning_limit" =~ ^([1-9]{1})([0-9]*)$ ]]; then
		pruning_limit="100"
	fi
	sudo echo "pruning_limit = $pruning_limit" >> "./electrum.conf"
	sudo echo "[reddcoind]" >> "./electrum.conf"
	sudo echo "host = $rpcallowip" >> "./electrum.conf"
	sudo echo "port = $rpcport" >> "./electrum.conf"
	sudo echo "user = $rpcuser" >> "./electrum.conf"
	sudo echo "password = $rpcpassword" >> "./electrum.conf"
	if [[ -e "$electrum_logfile_dir" ]]; then
		sudo rm "$electrum_logfile_dir"
	fi
	sudo touch "$electrum_logfile_dir"
	sudo chmod 666 "$electrum_logfile_dir"
	echo "Finish making electrum.conf."
	echo ""
}
function make_crt_key(){
	cd "$server_dir/reddcoin-electrum-server"
	openssl genrsa -des3 -passout pass:x -out ./server.pass.key 2048
	openssl rsa -passin pass:x -in ./server.pass.key -out ./server.key
	rm ./server.pass.key
	openssl req -new -key ./server.key -out ./server.csr
	openssl x509 -req -days 730 -in ./server.csr -signkey ./server.key -out ./server.crt
	echo "New certificate and its signing key created."
	echo ""
	cd -
}
function input_IP_address(){
	read -p "IP Address(default: 209.239.123.108): " rdd_IP_address
	if [[ -z "$rdd_IP_address" ]]; then
		rdd_IP_address="default"
	fi
	check_IP $rdd_IP_address
	valid_IP=$?
	if [[ $valid_IP == 0 ]]; then
		rdd_IP_address="209.239.123.108"
	fi
	read -p "Port number(default: 45444): " rdd_IP_port
	if [[ -z "$rdd_IP_port" ]]; then
		rdd_IP_port="45444"
	else
		check_port $rdd_IP_port
		valid_port=$?
		if [[ $valid_port == 0 ]]; then
			rdd_IP_port="default"
		fi
	fi
	sudo echo "addnode=$rdd_IP_address:$rdd_IP_port" >> "./reddcoin.conf"
}
function check_IP(){
	temp_arg="$1"
	if [[ ! "$temp_arg" =~ ^([0-9]{1,3})\.([0-9]{1,3})\.([0-9]{1,3})\.([0-9]{1,3}) ]]; then
		echo "Default IP would be used."
		return 0
	else
		IFS='.' read -ra position <<< "$temp_arg"
		for i in "${position[@]}"; do
			if [ $i -lt 0 -o $i -gt 255 ]; then
				return 0
			fi
		done
	fi
	return 1
}
function check_port(){
	temp_arg="$1"
	if [[ ! "$temp_arg" =~ ^([1-9]{1})([0-9]{0,4}){0,1}$ ]]; then
		echo "Default port would be used."
		return 0
	else
		if [ $temp_arg -lt 0 -o $temp_arg -gt 65535 ]; then
			return 0
		fi
	fi
	return 1
}
function configure_network(){
	echo "Configuring network settings..."
	tcp_port_cmd="sudo iptables -t nat -A PREROUTING -p tcp --dport $tcp_port -j DNAT --to $fqdn_IP:$tcp_port"
	eval $tcp_port_cmd
	sudo sysctl -w net.ipv4.conf.all.route_localnet=1
	echo "Configuration finished."
	echo ""
}
function create_execute_script(){
	electrum_config="$electrum_conf_dir/electrum.conf"
	cd "$server_dir/reddcoin-electrum-server"
	touch "./execute_electrum.sh"
	echo "#!/bin/bash" > "./execute_electrum.sh"
	echo "function read_config(){" >> "./execute_electrum.sh"
    echo "	text=\$1" >> "./execute_electrum.sh"
    echo "	echo \`grep -e ^$text $electrum_config |awk -F\= '{print $2}' | tail -n 1| tr -d ' '\`" >> "./execute_electrum.sh"
    echo "}" >> "./execute_electrum.sh"
	echo "tcp_port=\$(read_config \"stratum_tcp_port\")" >> "./execute_electrum.sh"
	echo "sudo iptables -t nat -A PREROUTING -p tcp --dport $tcp_port -j DNAT --to $fqdn_IP:$tcp_port" >> "./execute_electrum.sh"
	echo "sudo sysctl -w net.ipv4.conf.all.route_localnet=1" >> "./execute_electrum.sh"
	echo "sudo ./electrum-server start" >> "./execute_electrum.sh"
	sudo chmod 777 "./execute_electrum.sh"
	echo "Execution script finished."
	echo ""
}
reddcoind_dir="$HOME/Desktop/testDir"
server_dir="$HOME/Desktop/test1234567"
reddcoin_conf_dir="$HOME/.reddcoin"
#reddcoin_conf_dir="$HOME/Desktop/reddcoin2" 
#electrum_DB_dir="/var/electrum-server-test"
#electrum_conf_dir="$HOME/Desktop/reddcoin2"
electrum_DB_dir="/var/electrum-server"
electrum_conf_dir="/etc"
#electrum_logfile_dir="/var/log/electrum-test.log"
electrum_logfile_dir="/var/log/electrum.log"
rpcuser="rpcuser"
rpcpassword="123456"
rpcallowip="127.0.0.1"
rpcport="8332"
rdd_IP_address="209.239.123.108"
rdd_IP_port="45444"
fqdn_IP="127.0.0.1"
elect_rpc_port="8000"
tcp_port="50001"
tcp_ssl_port="50002"
http_port="8081"
http_ssl_port="8082"
echo "Before starting the script, please make sure that the reddcoind server v1.4.1.0,  v2.0.0.0 or v2.0.1.2 is installed."
while true; do
	read -p "reddcoind installed(Y/N): " installed
	if [[ ! -z "$installed" ]]; then 
		if [ $installed == "Y" -o $installed == "N" ]; then	
			break
		fi
	fi
done
if [[ $installed == "N" ]]; then
	echo "Please install reddcoind server from https://github.com/reddcoin-project/reddcoin/releases/tag/v2.0.1.2"
	exit
fi
read -p "Target Directory(default: $server_dir): " server_dir
if [[ -z "$server_dir" ]]; then
	server_dir="$HOME/Desktop/test1234567"
	mkdir -p $server_dir
else
	server_dir=${server_dir//"~"/$HOME}
fi
cd $server_dir
download_electrum_server
create_DB_dir
make_reddconf
make_electrumconf
create_execute_script
#configure_network
echo "Installation finished."
