package mods.immibis.microblocks.coremod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import net.minecraft.item.Item;

public class OptionsFile {
	public static final boolean DEBUG = false;
	
	public abstract static class Option {
		private final String name;
		public String getName() {
			return name;
		}
		
		public Option(String name) {
			this.name = name;
		}
		
		public abstract void setTo(String s) throws Exception;
		public abstract void finishReading() throws Exception;
		public abstract Collection<String> getValues();
	}
	
	public static class StringSetOption extends Option{
		private Set<String> readValues = new HashSet<String>();
		private Set<String> values = new HashSet<String>();
		
		private Collection<String> defaults;
		
		public StringSetOption(String name) {
			this(name, Collections.<String>emptySet());
		}
		
		public StringSetOption(String name, Set<String> defaults) {
			super(name);
			this.defaults = defaults;
			this.values.addAll(defaults);
		}
		
		@Override
		public void setTo(String s) throws Exception {
			if(s.equals(""))
				return;
			
			readValues.add(s);
		}
		
		@Override
		public void finishReading() throws Exception {
			values.clear();
			for(String s : readValues) {
				if(s.startsWith("+"))
					values.add(s.substring(1));
				else if(!s.startsWith("-"))
					values.add(s);
			}
			
			for(String s : defaults)
				if(!readValues.contains("-" + s))
					values.add(s);
		}
		
		@Override
		public Collection<String> getValues() {
			List<String> rv = new ArrayList<String>();
			
			for(String s : defaults)
				if(!values.contains(s))
					rv.add("-" + s);
				else
					rv.add("# included by default: "+getName()+": "+s);
			
			for(String s : values)
				if(!defaults.contains(s))
					rv.add("+" + s);
			
			return rv;
		}

		public Collection<? extends String> get() {
			return Collections.unmodifiableSet(values);
		}

		public void addValues(List<String> L) {
			values.addAll(L);
		}
	}
	
	public static class BooleanOption extends Option {
		
		private final boolean hasDefault, _default;
		private boolean hasValue;
		private boolean value;
		
		public BooleanOption(String name, boolean _default) {
			super(name);
			this.hasDefault = true;
			this._default = _default;
		}
		
		public BooleanOption(String name) {
			super(name);
			this.hasDefault = false;
			this._default = false;
		}
		
		@Override
		public void finishReading() throws Exception {
			if(!hasDefault && !hasValue)
				throw new Exception("value not specified");
			if(!hasValue)
				value = _default;
		}
		
		@Override
		public Collection<String> getValues() {
			return Arrays.asList(value ? "true" : "false");
		}
		
		@Override
		public void setTo(String s) throws Exception {
			
			if(hasValue)
				throw new Exception("value already specified");
			hasValue = true;
			
			if(s.equals("true"))
				value = true;
			else if(s.equals("false"))
				value = false;
			else
				throw new Exception("must be true or false");
		}

		public void set(boolean v) {
			if(DEBUG)
				System.out.println("[Immibis's Microblocks DEBUG] BooleanOption "+getName()+": setting to "+v+", was "+value); 
			value = v;
		}

		public boolean get() {
			return value;
		}
	}
	
	public static class ItemListOption extends Option {
		public ItemListOption(String name) {
			super(name);
		}

		protected boolean requireMeta; 
		public static class ItemID {
			public String id;
			public int meta;
			public boolean hasMeta;
			
			public ItemID(String id) {
				this.id = id;
			}
			
			public ItemID(String id, int meta) {
				this.id = id;
				this.meta = meta;
				this.hasMeta = true;
			}
			
			public ItemID(String line, boolean requireMeta) throws Exception {
				if(line.contains("#"))
					line = line.substring(0, line.indexOf('#')).trim();
				
				int i = line.lastIndexOf(':');
				if(i <= 0) {
					if(requireMeta)
						throw new Exception("damage value is required, format is <id>:<damage value> eg 5:2");
					this.id = line;
				} else {
					try {
						this.id = line.substring(0, i);
						this.meta = Integer.parseInt(line.substring(i+1));
						this.hasMeta = true;
					} catch(NumberFormatException e) {
						throw new Exception("invalid number. must be in <id> or <id>:<damage value> format, eg 5:2");
					}
				}
				
				if(hasMeta && (meta < -32768 || meta > 32767))
					throw new Exception("damage value outside valid range");
			}
			
			public ItemID(net.minecraft.item.Item item, int meta2) {
				this(Item.itemRegistry.getNameForObject(item), meta2);
			}
			
			@Override
			public String toString() {
				String s;
				if(!hasMeta)
					s = String.valueOf(id);
				else
					s = id + ":" + meta;
				
				if(BridgeClass1.isMinecraftLoaded)
					s = s + "    # " + BridgeClass2.getItemName((Item)Item.itemRegistry.getObject(id), hasMeta ? meta : 0);
				
				return s;
			}
		}
		
		private List<ItemID> values = new ArrayList<ItemID>();
		
		@Override
		public void finishReading() throws Exception {
			
		}
		
		@Override
		public Collection<String> getValues() {
			List<String> rv = new ArrayList<String>(values.size());
			for(ItemID id : values)
				rv.add(id.toString());
			return rv;
		}
		
		@Override
		public void setTo(String s) throws Exception {
			values.add(new ItemID(s, requireMeta));
		}

		public List<ItemID> get() {
			return values;
		}
		
		public void set(List<ItemID> v) {
			v = new ArrayList<ItemID>(v); // in case v == values
			
			if(DEBUG)
				System.out.println("[Immibis's Microblocks DEBUG] ItemListOption "+getName()+": setting to "+v+", was "+values);
			values.clear();
			values.addAll(v);
		}
	}
	
	public static class ItemAndMetaListOption extends ItemListOption {
		public ItemAndMetaListOption(String name) {
			super(name);
		}

		{
			requireMeta = true;
		}
	}
	
	private Map<String, Option> options = new HashMap<String, Option>();
	
	public void addOption(Option option) {
		String name = option.getName();
		
		if(options.containsKey(name))
			throw new IllegalArgumentException("option already added with name: "+name);
		
		options.put(name, option);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOption(String name) {
		return (T)options.get(name);
	}
	
	public void read(File f) throws IOException {
		
		if(DEBUG)
			System.out.println("[Immibis's Microblocks DEBUG] Reading config file from "+f);
		
		BufferedReader in = new BufferedReader(new FileReader(f));
		try {
			String line;
			int lineNo = 0;
			while((line = in.readLine()) != null) {
				lineNo++;
				
				line = line.trim();
				if(line.startsWith("#") || line.equals(""))
					continue;
				
				int i = line.indexOf(':');
				if(i < 0)
					throw new IOException("config error: malformed line, on line "+lineNo+" of "+f.getName());
				
				String key = line.substring(0, i).trim();
				String value = line.substring(i+1).trim();
				
				Option opt = options.get(key);
				if(opt == null)
					throw new IOException("config error: unknown option, on line "+lineNo+" of "+f.getName());
				
				try {
					opt.setTo(value);
					
				} catch(Exception e) {
					if(e.getClass() == Exception.class)
						throw new IOException("config error: "+e.getMessage()+", on line "+lineNo+" of "+f.getName());
					else
						throw new IOException("config error: internal exception, on line "+lineNo+" of "+f.getName(), e);
				}
			}
			
			for(Map.Entry<String, Option> e : options.entrySet()) {
				try {
					e.getValue().finishReading();
				} catch(Exception ex) {
					if(ex.getClass() == Exception.class)
						throw new IOException("config error: "+ex.getMessage()+", in option "+e.getKey()+" of "+f.getName());
					else
						throw new IOException("config error: internal exception, in option "+e.getKey()+" of "+f.getName(), ex);
				}
			}
			
		} finally {
			in.close();
		}
		
		if(DEBUG)
			System.out.println("[Immibis's Microblocks DEBUG] Finished reading config file");
	}

	public void write(File f) throws IOException {
		
		if(DEBUG)
			System.out.println("[Immibis's Microblocks DEBUG] Writing config file to "+f);
		
		Writer out = new FileWriter(f);
		
		String lineSep = System.getProperty("line.separator");
		
		try {
			for(Map.Entry<String, Option> e : options.entrySet())
				out.write("# "+e.getKey()+" is a "+e.getValue().getClass().getSimpleName()+lineSep);
			
			out.write(lineSep);
			out.write("# This is the format to add an entry to a StringSetOption:"+lineSep);
			out.write("#   blockClass: +some.block.class.name"+lineSep);
			out.write("# That will add some.block.class.name as a blockClass."+lineSep);
			out.write("# If something is included by default and you want to remove it, use this format:"+lineSep);
			out.write("#   tileEntityClass: -appeng.me.tile.TileOutputCable"+lineSep);
			out.write("# That will remove appeng.me.tile.TileOutputCable as a tileEntityClass."+lineSep);
			out.write(lineSep);
			out.write("# AUTODETECT IS BROKEN ON SERVERS!"+lineSep);
			out.write("# If you're on a server, don't do that. Use autodetect on your client instead, then copy the config file to the server."+lineSep);
			out.write(lineSep);
			
			for(Map.Entry<String, Option> e : options.entrySet()) {
				Collection<String> values = e.getValue().getValues();
				if(values.size() > 0)
					out.write(lineSep);
				for(String val : values)
					if(val.startsWith("#"))
						out.write(val + lineSep);
					else
						out.write(e.getKey()+": "+val+lineSep);
			}
			
		} finally {
			out.close();
		}
		
		if(DEBUG)
			System.out.println("[Immibis's Microblocks DEBUG] Finished writing config file");
	}
	
}
