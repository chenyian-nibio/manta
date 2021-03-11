update sample set has_metadata = true  
where id in (select distinct sample_id from parameter_value );

update sample set has_16s = true  
where id in (select distinct sample_id from microbiota where microbiota.method_id = 1 );

update sample set has_shotgun = true  
where id in (select distinct sample_id from microbiota where microbiota.method_id = 2 );
