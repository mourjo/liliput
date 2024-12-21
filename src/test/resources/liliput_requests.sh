#!/bin/bash
counter=41218
while [ $counter -gt 0 ]
do
    echo "*******************************************************";
    echo "Remaining runs $counter ($(date))" ; 
    echo "*******************************************************";
    counter=$(( counter - 1 )) ;
    
    
    curl -i https://liliput.mourjo.me/l/G4LZ1a ;
    sleep 30;
    printf "\n";
    
    
    curl -Iis 'https://liliput.mourjo.me/?code=5874bc0c-0a13-4ab7-9422-18dfa6918d4d' -o /dev/null;
    sleep 30;
    printf "\n";
    
    
    curl -Iis 'https://liliput.mourjo.me' -o /dev/null;
    
    
    curl -i -s -X POST 'https://liliput.mourjo.me/link' \
  -H 'accept: */*' \
  -H 'accept-language: en-US,en;q=0.9,bn;q=0.8,fr;q=0.7' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'cookie: _ga=GA1.1.1034247688.1715178708; _ga_WLBRKMS83E=GS1.1.1719944314.10.0.1719944739.0.0.0; refresh_token=XXX; access_token=YYY; id_token=ZZZ;' \
  -H 'dnt: 1' \
  -H 'origin: https://liliput.mourjo.me' \
  -H 'pragma: no-cache' \
  -H 'priority: u=1, i' \
  -H 'referer: https://liliput.mourjo.me/' \
  -H 'sec-ch-ua: "Chromium";v="130", "Google Chrome";v="130", "Not?A_Brand";v="99"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "macOS"' \
  -H 'sec-fetch-dest: empty' \
  -H 'sec-fetch-mode: cors' \
  -H 'sec-fetch-site: same-origin' \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36' \
  --data-raw '{"link":"https://www.booking.com/hotel/nl/park-inn-by-radisson-amsterdam-city-west.html?aid=304142"}';
    sleep 30; 
    printf "\n" ;


    curl -i 'https://liliput.mourjo.me/links' \
  -H 'accept: */*' \
  -H 'accept-language: en-US,en;q=0.9,bn;q=0.8,fr;q=0.7' \
  -H 'cache-control: no-cache' \
  -H 'cookie: _ga=GA1.1.1034247688.1715178708; _ga_WLBRKMS83E=GS1.1.1719944314.10.0.1719944739.0.0.0; refresh_token=XXX; access_token=YYY; id_token=ZZZ;' \
  -H 'dnt: 1' \
  -H 'pragma: no-cache' \
  -H 'priority: u=1, i' \
  -H 'referer: https://liliput.mourjo.me/' \
  -H 'sec-ch-ua: "Chromium";v="130", "Google Chrome";v="130", "Not?A_Brand";v="99"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "macOS"' \
  -H 'sec-fetch-dest: empty' \
  -H 'sec-fetch-mode: cors' \
  -H 'sec-fetch-site: same-origin' \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36' ;
    printf "\n\n\n";
    sleep 120;
done
